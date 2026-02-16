package digital.guimauve.pkg.domain.usecases.users

import dev.kaccelero.usecases.IPairSuspendUseCase
import digital.guimauve.pkg.models.users.User

interface IGetUserForEmailUseCase : IPairSuspendUseCase<String, Boolean, User?>
