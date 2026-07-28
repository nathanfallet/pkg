package digital.guimauve.pkg.presentation.mappers.users

import digital.guimauve.pkg.api.responses.users.UserResponse
import digital.guimauve.pkg.models.users.User

/**
 * Maps a [User] to a [UserResponse].
 *
 * @return The mapped [UserResponse].
 */
fun User.toUserResponse() = UserResponse(
    id = id,
    organizationId = organizationId,
    email = email,
)
