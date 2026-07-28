package digital.guimauve.pkg.presentation.extensions

import digital.guimauve.pkg.domain.exceptions.auth.InvalidCredentialsException
import digital.guimauve.pkg.domain.exceptions.auth.InvalidTokenException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationForbiddenException
import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationNotFoundException
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.models.auth.SessionPayload
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.users.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import kotlin.uuid.Uuid

/**
 * Retrieves the session of the application call.
 *
 * @return The [SessionPayload] if the call carries a session, otherwise null.
 */
fun ApplicationCall.session(): SessionPayload? = sessions.get()

/**
 * Starts a session on the application call.
 *
 * @param payload The session to store.
 */
fun ApplicationCall.setSession(payload: SessionPayload) = sessions.set(payload)

/**
 * Ends the session of the application call.
 */
fun ApplicationCall.clearSession() = sessions.clear<SessionPayload>()

/**
 * Retrieves the user ID of the application call, from any of the ways it can be authenticated:
 * a JWT (API), a basic or bearer credential (package registries), or a session cookie (dashboard).
 *
 * @return The user ID as a [Uuid] if present, otherwise null.
 */
fun ApplicationCall.userIdOrNull(): Uuid? =
    principal<JWTPrincipal>()?.payload?.subject?.let(Uuid::parse)
        ?: principal<UserIdPrincipal>()?.name?.let(Uuid::parse)
        ?: session()?.userId

/**
 * Retrieves the user ID of the application call.
 *
 * @return The user ID as a [Uuid].
 * @throws InvalidTokenException if the call is not authenticated.
 */
fun ApplicationCall.userId(): Uuid = userIdOrNull() ?: throw InvalidTokenException()

private data class UserForCall(
    val user: User?,
)

private val userKey = AttributeKey<UserForCall>("user")

/**
 * Retrieves the user of the application call, looking it up only once per call.
 *
 * @param getUserUseCase The use case resolving a user from its ID.
 *
 * @return The [User] if the call is authenticated, otherwise null.
 */
suspend fun ApplicationCall.userOrNull(getUserUseCase: GetUserUseCase): User? =
    // Note: we cannot use `computeIfAbsent` because it does not support suspending functions
    attributes.getOrNull(userKey)?.user ?: run {
        val computed = UserForCall(userIdOrNull()?.let { getUserUseCase(it) })
        attributes.put(userKey, computed)
        computed.user
    }

/**
 * Retrieves the user of the application call.
 *
 * @param getUserUseCase The use case resolving a user from its ID.
 *
 * @return The [User].
 * @throws InvalidCredentialsException if the call is not authenticated.
 */
suspend fun ApplicationCall.requireUser(getUserUseCase: GetUserUseCase): User =
    userOrNull(getUserUseCase) ?: throw InvalidCredentialsException()

private data class OrganizationForCall(
    val organization: Organization?,
)

private val organizationKey = AttributeKey<OrganizationForCall>("organization")

/**
 * Retrieves the organization the user of the application call belongs to, looking it up only once per call.
 *
 * @param getUserUseCase The use case resolving a user from its ID.
 * @param getOrganizationUseCase The use case resolving an organization from its ID.
 *
 * @return The [Organization].
 * @throws InvalidCredentialsException if the call is not authenticated.
 * @throws OrganizationNotFoundException if the organization does not exist.
 */
suspend fun ApplicationCall.requireOrganization(
    getUserUseCase: GetUserUseCase,
    getOrganizationUseCase: GetOrganizationUseCase,
): Organization =
    // Note: we cannot use `computeIfAbsent` because it does not support suspending functions
    attributes.getOrNull(organizationKey)?.organization ?: run {
        val computed = OrganizationForCall(getOrganizationUseCase(requireUser(getUserUseCase).organizationId))
        attributes.put(organizationKey, computed)
        computed.organization
    } ?: throw OrganizationNotFoundException()

/**
 * Retrieves the organization of the given ID, which the user of the application call must belong to.
 * A user only ever has access to its own organization.
 *
 * @param organizationId The ID of the organization being accessed.
 * @param getUserUseCase The use case resolving a user from its ID.
 * @param getOrganizationUseCase The use case resolving an organization from its ID.
 *
 * @return The [Organization].
 * @throws InvalidCredentialsException if the call is not authenticated.
 * @throws OrganizationNotFoundException if the organization does not exist.
 * @throws OrganizationForbiddenException if the user does not belong to that organization.
 */
suspend fun ApplicationCall.requireOrganization(
    organizationId: Uuid,
    getUserUseCase: GetUserUseCase,
    getOrganizationUseCase: GetOrganizationUseCase,
): Organization = requireOrganization(getUserUseCase, getOrganizationUseCase)
    .takeIf { it.id == organizationId } ?: throw OrganizationForbiddenException()
