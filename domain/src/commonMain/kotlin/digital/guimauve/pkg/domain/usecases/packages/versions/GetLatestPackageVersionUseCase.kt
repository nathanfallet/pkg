package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

interface GetLatestPackageVersionUseCase {
    suspend operator fun invoke(versionId: Uuid): PackageVersion?
}
