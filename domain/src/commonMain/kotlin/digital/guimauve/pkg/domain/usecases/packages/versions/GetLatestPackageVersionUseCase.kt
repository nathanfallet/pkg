package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface GetLatestPackageVersionUseCase {
    suspend operator fun invoke(versionId: Uuid): PackageVersion?
}
