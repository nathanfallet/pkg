package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.models.UUID
import dev.kaccelero.usecases.ISuspendUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion

interface IGetLatestPackageVersionUseCase : ISuspendUseCase<UUID, PackageVersion?>
