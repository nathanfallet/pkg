package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

/**
 * Reads and writes the users. The password hash is left out ([User.password] is null) unless
 * [getForEmail] is explicitly asked for it.
 */
interface UsersRepository {

    /** Lists the users of an organization, in no particular order. */
    suspend fun list(organizationId: Uuid): List<User>

    /** Reads a user by its identifier, whatever its organization. */
    suspend fun get(id: Uuid): User?

    /** Reads a user by its identifier. @return The user, or null if it doesn't exist in that organization. */
    suspend fun get(id: Uuid, organizationId: Uuid): User?

    /**
     * Reads a user by its email address (the first match: the column is not unique).
     *
     * @param includePassword Whether to fill [User.password] with the stored hash, which only the
     * login needs.
     */
    suspend fun getForEmail(email: String, includePassword: Boolean): User?

    /**
     * Registers a user in an organization. [CreateUserPayload.password] is stored as given, so it
     * must already be encoded.
     *
     * @return The created user (without its password), or null if the insert returned no row.
     */
    suspend fun create(payload: CreateUserPayload, organizationId: Uuid): User?

}
