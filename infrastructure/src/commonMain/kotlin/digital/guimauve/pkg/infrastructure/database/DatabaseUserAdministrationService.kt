package digital.guimauve.pkg.infrastructure.database

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.services.*
import digital.guimauve.pkg.infrastructure.database.tables.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.security.SecureRandom
import java.util.Base64
import kotlin.uuid.Uuid

class DatabaseUserAdministrationService(
    private val transactions: TransactionManager,
    private val passwords: PasswordEncoderService,
) : UserAdministrationService {
    private val random = SecureRandom()
    private fun secret(): String = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(ByteArray(24).also(random::nextBytes))

    private fun lock() {
        check(AdministrationLock.selectAll().where { AdministrationLock.id eq 1 }.forUpdate().single()[AdministrationLock.id] == 1)
    }

    /** The returned password may be logged only AFTER this transaction commits. */
    fun bootstrap(): TemporaryPassword? = transactions.transaction {
        lock()
        if (Users.selectAll().limit(1).any()) return@transaction null
        val organizationId = Organizations.insert { it[name] = "Default" }[Organizations.id].value
        createAccount("admin", organizationId, true)
    }

    private fun user(id: Uuid): User = Users.selectAll().where { Users.id eq id }
        .map { Users.toUser(it, true) }.singleOrNull() ?: throw AccountFailure(404, "users_not_found")

    private fun fresh(actor: User, requireAdmin: Boolean = true): User {
        val current = user(actor.id)
        if (current.disabled || current.credentialVersion != actor.credentialVersion)
            throw AccountFailure(401, "auth_invalid_credentials")
        if (requireAdmin && (!current.admin || current.passwordChangeRequired))
            throw AccountFailure(403, "auth_admin_required")
        return current
    }

    override suspend fun overview(actor: User): AdministrationOverview = transactions.suspendTransaction {
        fresh(actor)
        AdministrationOverview(
            Users.selectAll().orderBy(Users.email).map { Users.toUser(it) },
            Organizations.selectAll().map(Organizations::toOrganization),
        )
    }

    private fun createAccount(username: String, organizationId: Uuid, admin: Boolean): TemporaryPassword {
        val password = secret()
        val row = Users.insert {
            it[Users.organizationId] = organizationId
            // Keep the historical column/API name so existing email logins remain unchanged.
            it[email] = username
            it[Users.password] = passwords.encode(password)
            it[Users.admin] = admin
            it[passwordChangeRequired] = true
        }.resultedValues!!.single()
        return TemporaryPassword(Users.toUser(row), password)
    }

    override suspend fun create(actor: User, username: String, organizationId: Uuid, admin: Boolean): TemporaryPassword =
        transactions.suspendTransaction {
            lock()
            fresh(actor)
            if (!Regex("[a-zA-Z0-9][a-zA-Z0-9._@+-]{2,254}").matches(username))
                throw AccountFailure(400, "auth_username_invalid")
            if (Users.selectAll().where { Users.email eq username }.any())
                throw AccountFailure(409, "auth_username_exists")
            if (!Organizations.selectAll().where { Organizations.id eq organizationId }.any())
                throw AccountFailure(404, "organizations_not_found")
            createAccount(username, organizationId, admin)
        }

    override suspend fun update(actor: User, userId: Uuid, admin: Boolean, disabled: Boolean) {
        transactions.suspendTransaction {
            lock()
            fresh(actor)
            val target = user(userId)
            if (target.admin && !target.disabled && (!admin || disabled) &&
                Users.selectAll().where { (Users.admin eq true) and (Users.disabled eq false) }.count() <= 1)
                throw AccountFailure(409, "auth_last_admin")
            Users.update({ Users.id eq userId }) {
                it[Users.admin] = admin
                it[Users.disabled] = disabled
                if (target.admin != admin || target.disabled != disabled)
                    it[credentialVersion] = target.credentialVersion + 1
            }
        }
    }

    override suspend fun resetPassword(actor: User, userId: Uuid): TemporaryPassword = transactions.suspendTransaction {
        lock()
        fresh(actor)
        val target = user(userId)
        val password = secret()
        Users.update({ Users.id eq userId }) {
            it[Users.password] = passwords.encode(password)
            it[passwordChangeRequired] = true
            it[credentialVersion] = target.credentialVersion + 1
        }
        TemporaryPassword(user(userId).copy(password = null), password)
    }

    override suspend fun changePassword(actor: User, current: String, replacement: String) {
        // BCrypt accepts at most 72 UTF-8 bytes; reject longer values rather than truncating them.
        if (replacement.length < 14 || replacement.encodeToByteArray().size > 72 || replacement == current)
            throw AccountFailure(400, "auth_password_invalid")
        transactions.suspendTransaction {
            lock()
            val target = fresh(actor, requireAdmin = false)
            if (current.encodeToByteArray().size > 72 || !passwords.matches(current, target.password!!))
                throw AccountFailure(401, "auth_invalid_credentials")
            Users.update({ Users.id eq actor.id }) {
                it[password] = passwords.encode(replacement)
                it[passwordChangeRequired] = false
                it[credentialVersion] = target.credentialVersion + 1
            }
        }
    }

    /** Version zero also accepts pre-upgrade JWTs; later versions revoke every older credential. */
    fun credentialVersion(id: Uuid): Int? = transactions.transaction {
        Users.selectAll().where { Users.id eq id }.singleOrNull()?.let { row ->
            row[Users.credentialVersion].takeUnless { row[Users.disabled] || row[Users.passwordChangeRequired] }
        }
    }
}
