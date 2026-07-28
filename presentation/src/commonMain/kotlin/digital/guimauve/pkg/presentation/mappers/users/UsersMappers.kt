package digital.guimauve.pkg.presentation.mappers.users

import digital.guimauve.pkg.api.responses.users.UserResponse
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.presentation.views.UserView

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

/**
 * Maps a [User] to a [UserView].
 *
 * @return The mapped [UserView].
 */
fun User.toUserView() = UserView(
    id = id.toString(),
    email = email,
    organizationId = organizationId.toString(),
    url = "/users/$id",
)
