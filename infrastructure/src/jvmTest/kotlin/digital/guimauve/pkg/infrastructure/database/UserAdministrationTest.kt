package digital.guimauve.pkg.infrastructure.database

import digital.guimauve.pkg.domain.services.*
import digital.guimauve.pkg.infrastructure.database.repositories.UsersDatabaseRepository
import digital.guimauve.pkg.infrastructure.database.tables.*
import digital.guimauve.pkg.infrastructure.jwt.JwtTokenService
import digital.guimauve.pkg.domain.models.auth.TokenType
import kotlinx.coroutines.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.core.eq
import java.nio.file.Files
import kotlin.test.*
import kotlin.uuid.Uuid

class UserAdministrationTest {
    private val encoder = object : PasswordEncoderService {
        override fun encode(rawPassword: String) = "test-hash:$rawPassword"
        override fun matches(rawPassword: String, encodedPassword: String) = encode(rawPassword) == encodedPassword
    }

    private fun factory() = H2DatabaseFactory(DatabaseConfig(protocol = "h2", name = "accounts-${Uuid.random()}"))
        .also { it.migrate() }

    @Test
    fun bootstrapIsSerializedAcrossInstancesAndSurvivesRestart() = runBlocking {
        val directory = Files.createTempDirectory("pkg-bootstrap-").toFile()
        try {
            val config = DatabaseConfig(protocol = "h2", name = "persistent", directory = directory.absolutePath)
            val first = H2DatabaseFactory(config).also { it.migrate() }
            val created = coroutineScope {
                (1..4).map { async(Dispatchers.IO) {
                    DatabaseUserAdministrationService(TransactionManagerImpl(H2DatabaseFactory(config)), encoder).bootstrap()
                } }.awaitAll()
            }.filterNotNull()
            val initial = created.single()
            assertEquals("admin", initial.user.email)
            assertTrue(initial.user.admin)
            assertTrue(initial.user.passwordChangeRequired)
            assertFalse(initial.toString().contains(initial.password))
            val transactions = TransactionManagerImpl(first)
            val service = DatabaseUserAdministrationService(transactions, encoder)
            service.changePassword(initial.user, initial.password, "a permanent password")
            val restarted = DatabaseUserAdministrationService(TransactionManagerImpl(H2DatabaseFactory(config)), encoder)
            assertNull(restarted.bootstrap())
            val admin = UsersDatabaseRepository(transactions).getForEmail("admin", true)!!
            assertTrue(encoder.matches("a permanent password", admin.password!!))
            assertFalse(admin.passwordChangeRequired)
            transactions.transaction { assertEquals(1, Organizations.selectAll().count()) }
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun upgradeLeavesExistingAccountsAndLegacyTokensUntouched() = runBlocking {
        val factory = H2DatabaseFactory(DatabaseConfig(protocol = "h2", name = "legacy-${Uuid.random()}"))
        val transactions = TransactionManagerImpl(factory)
        org.jetbrains.exposed.v1.jdbc.transactions.transaction(factory.getDatabase()) {
            val baseline = checkNotNull(javaClass.classLoader.getResource("db/migration/V1__baseline.sql")).readText()
            baseline.lineSequence().filterNot { it.trimStart().startsWith("--") }.joinToString("\n")
                .split(';').filter { it.isNotBlank() }.forEach { exec(it) }
            exec("INSERT INTO Users (id, organization_id, email, password) VALUES (X'00000000000000000000000000000001', X'00000000000000000000000000000002', 'existing@example.com', 'test-hash:old password')")
        }
        factory.migrate()
        val repository = UsersDatabaseRepository(transactions)
        val old = repository.getForEmail("existing@example.com", false)!!
        val service = DatabaseUserAdministrationService(transactions, encoder)
        assertNull(service.bootstrap())
        val stored = repository.get(old.id)!!
        assertFalse(stored.admin)
        assertFalse(stored.disabled)
        assertFalse(stored.passwordChangeRequired)
        assertEquals(0, stored.credentialVersion)
        assertEquals(encoder.encode("old password"), repository.getForEmail(old.email, true)!!.password)
        // Exact pre-upgrade token shape: no credential_version claim.
        val legacy = com.auth0.jwt.JWT.create().withSubject(old.id.toString()).withIssuer("pkg").withAudience("pkg")
            .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test"))
        val tokens = JwtTokenService("test", "pkg", "pkg", credentialVersion = service::credentialVersion)
        assertEquals(old.id, tokens.verifyToken(legacy))
        transactions.transaction { Users.update({ Users.id eq old.id }) { it[admin] = true } }
        assertEquals(old.id, tokens.verifyToken(legacy), "manual promotion must preserve access")
    }

    @Test
    fun administrationEnforcesPermissionsAndRevokesCredentials() = runBlocking {
        val transactions = TransactionManagerImpl(factory())
        val service = DatabaseUserAdministrationService(transactions, encoder)
        val repository = UsersDatabaseRepository(transactions)
        val initial = service.bootstrap()!!
        assertFailsWith<AccountFailure> { service.overview(initial.user) }
        service.changePassword(initial.user, initial.password, "admin permanent password")
        val admin = repository.get(initial.user.id)!!
        val secondOrganization = transactions.transaction {
            Organizations.insert { it[name] = "Existing organization" }[Organizations.id].value
        }
        val created = service.create(admin, "alice", secondOrganization, false)
        assertTrue(created.user.passwordChangeRequired)
        assertNull(service.credentialVersion(created.user.id))
        assertFailsWith<AccountFailure> { service.create(admin, "alice", secondOrganization, false) }
        assertFailsWith<AccountFailure> { service.changePassword(created.user, "wrong", "member permanent password") }
        assertFailsWith<AccountFailure> { service.changePassword(created.user, created.password, created.password) }
        assertFailsWith<AccountFailure> { service.changePassword(created.user, created.password, "é".repeat(37)) }
        service.changePassword(created.user, created.password, "member permanent password")
        val alice = repository.get(created.user.id)!!
        assertEquals(secondOrganization, alice.organizationId)
        assertFailsWith<AccountFailure> { service.overview(alice) }
        assertFailsWith<AccountFailure> { service.create(alice, "eve", secondOrganization, true) }
        assertFailsWith<AccountFailure> { service.update(alice, admin.id, false, true) }
        assertFailsWith<AccountFailure> { service.resetPassword(alice, admin.id) }
        assertFailsWith<AccountFailure> { service.update(admin, admin.id, false, false) }
        assertFailsWith<AccountFailure> { service.update(admin, admin.id, true, true) }
        val tokens = JwtTokenService("test", "pkg", "pkg", credentialVersion = service::credentialVersion)
        val token = tokens.generateToken(alice.id, TokenType.ACCESS)
        assertEquals(alice.id, tokens.verifyToken(token))
        service.update(admin, alice.id, false, true)
        assertNull(tokens.verifyToken(token))
        assertFailsWith<AccountFailure> { service.changePassword(alice, "member permanent password", "another permanent password") }
        service.update(admin, alice.id, false, false)
        assertNull(tokens.verifyToken(token), "re-enabling cannot resurrect old tokens")
        val nextToken = tokens.generateToken(alice.id, TokenType.ACCESS)
        val reset = service.resetPassword(admin, alice.id)
        assertNull(tokens.verifyToken(nextToken))
        assertTrue(reset.user.passwordChangeRequired)
        assertFalse(reset.password == created.password)
        assertFalse(service.overview(admin).toString().contains(reset.password))
        service.changePassword(reset.user, reset.password, "another permanent password")
        assertNull(tokens.verifyToken(nextToken))
        assertFailsWith<digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException> {
            tokens.generateToken(alice.id, TokenType.ACCESS, alice.credentialVersion)
        }
    }

    @Test
    fun concurrentDemotionsCannotRemoveEveryAdministrator() = runBlocking {
        val transactions = TransactionManagerImpl(factory())
        val service = DatabaseUserAdministrationService(transactions, encoder)
        val repository = UsersDatabaseRepository(transactions)
        val initial = service.bootstrap()!!
        service.changePassword(initial.user, initial.password, "first admin password")
        val first = repository.get(initial.user.id)!!
        val created = service.create(first, "another-admin", first.organizationId, true)
        service.changePassword(created.user, created.password, "second admin password")
        val second = repository.get(created.user.id)!!
        val results = coroutineScope {
            listOf(first, second).map { actor -> async {
                runCatching { service.update(actor, actor.id, false, false) }
            } }.awaitAll()
        }
        assertEquals(1, results.count { it.isSuccess })
        transactions.transaction { assertEquals(1, Users.selectAll().where { Users.admin eq true }.count()) }
    }
}
