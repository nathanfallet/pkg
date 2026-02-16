package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.usecases.ITripleSuspendUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.users.User
import kotlin.uuid.Uuid

interface IGetOrCreatePackageVersionUseCase : ITripleSuspendUseCase<String, Uuid, User, PackageVersion?>
