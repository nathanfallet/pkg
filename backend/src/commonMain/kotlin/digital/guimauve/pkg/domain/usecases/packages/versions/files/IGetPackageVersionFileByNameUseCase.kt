package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.models.UUID
import dev.kaccelero.usecases.IPairSuspendUseCase
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile

interface IGetPackageVersionFileByNameUseCase : IPairSuspendUseCase<String, UUID, PackageVersionFile?>
