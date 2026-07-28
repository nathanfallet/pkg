package digital.guimauve.pkg.presentation.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import digital.guimauve.pkg.domain.services.TranslateService
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.infrastructure.i18n.PropertiesTranslateService
import digital.guimauve.pkg.models.auth.SessionPayload
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.presentation.config.configureErrorHandling
import digital.guimauve.pkg.presentation.config.configureSerialization
import digital.guimauve.pkg.presentation.config.configureTemplating
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.*
import io.ktor.server.sessions.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * Everything the route tests need to stand up a Ktor application holding a single routes file,
 * with mocked use cases behind it.
 */
object RoutesTestHelper {

    val TEST_USER_ID: Uuid = Uuid.parse("00000000-0000-4000-8000-000000000001")
    val TEST_ORGANIZATION_ID: Uuid = Uuid.parse("00000000-0000-4000-8000-000000000002")
    val TEST_PACKAGE_ID: Uuid = Uuid.parse("00000000-0000-4000-8000-000000000003")
    val TEST_VERSION_ID: Uuid = Uuid.parse("00000000-0000-4000-8000-000000000004")

    private const val TEST_SECRET = "test-secret-key-for-jwt-testing-purposes"

    val testUser = User(
        id = TEST_USER_ID,
        organizationId = TEST_ORGANIZATION_ID,
        email = "test@guimauve.digital",
        password = null,
    )

    val testOrganization = Organization(
        id = TEST_ORGANIZATION_ID,
        name = "Guimauve Digital",
    )

    val testPackage = Package(
        id = TEST_PACKAGE_ID,
        name = "com.example:library",
        format = PackageFormat.MAVEN,
        organizationId = TEST_ORGANIZATION_ID,
        isPublic = true,
        createdAt = Instant.parse("2026-01-02T03:04:05Z"),
    )

    /**
     * Signs a JWT the test application accepts, as the API clients would send.
     */
    fun generateTestToken(userId: Uuid = TEST_USER_ID): String = JWT.create()
        .withSubject(userId.toString())
        .withIssuer("test")
        .withAudience("test")
        .sign(Algorithm.HMAC256(TEST_SECRET))

    /**
     * Installs everything the routes rely on outside of their own handlers: the real serialization,
     * templating and error mapping, plus a JWT provider and cookie sessions holding no state.
     *
     * @param getUserUseCase The mock the `call.userOrNull(...)` extension resolves users through.
     */
    fun Application.configureTestApplication(getUserUseCase: GetUserUseCase) {
        install(Koin) {
            modules(
                module {
                    single<TranslateService> { PropertiesTranslateService() }
                    single { getUserUseCase }
                }
            )
        }
        authentication {
            jwt("api-jwt") {
                verifier(JWT.require(Algorithm.HMAC256(TEST_SECRET)).build())
                validate { credential -> credential.payload.subject?.let { JWTPrincipal(credential.payload) } }
            }
        }
        install(Sessions) {
            cookie<SessionPayload>("session")
        }
        install(Resources)
        configureSerialization()
        configureTemplating()
        configureErrorHandling()
    }

}
