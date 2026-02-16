package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.usecases.IPairSuspendUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface IGetPackageVersionByNameUseCase : IPairSuspendUseCase<String, Uuid, PackageVersion?>
