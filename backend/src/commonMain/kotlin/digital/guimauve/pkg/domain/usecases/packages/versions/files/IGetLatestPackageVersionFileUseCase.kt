package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.usecases.IPairSuspendUseCase
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import kotlin.uuid.Uuid

interface IGetLatestPackageVersionFileUseCase : IPairSuspendUseCase<String, Uuid, PackageVersionFile?>
