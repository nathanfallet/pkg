package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.usecases.ISuspendUseCase
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface IGetLatestPackageVersionUseCase : ISuspendUseCase<Uuid, PackageVersion?>
