package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.models.UUID
import dev.kaccelero.usecases.ISuspendUseCase
import digital.guimauve.pkg.models.users.User

interface IGetUserUseCase : ISuspendUseCase<UUID, User?>
