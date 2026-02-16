package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.models.UUID
import dev.kaccelero.usecases.ITripleSuspendUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.users.User

interface IGetOrCreatePackageVersionUseCase : ITripleSuspendUseCase<String, UUID, User, PackageVersion?>
