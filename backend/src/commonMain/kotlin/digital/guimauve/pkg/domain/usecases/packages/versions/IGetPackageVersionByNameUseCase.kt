package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.models.UUID
import dev.kaccelero.usecases.IPairSuspendUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion

interface IGetPackageVersionByNameUseCase : IPairSuspendUseCase<String, UUID, PackageVersion?>
