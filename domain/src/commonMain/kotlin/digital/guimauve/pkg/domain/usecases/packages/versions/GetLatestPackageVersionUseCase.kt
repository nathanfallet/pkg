package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.uuid.Uuid

/**
 * Reads the most recently published version of a package (by publication date, yanked versions
 * included), or null if it has none.
 *
 * Despite its name, the parameter is the identifier of the **package**, not of a version.
 */
interface GetLatestPackageVersionUseCase {
    suspend operator fun invoke(versionId: Uuid): PackageVersion?
}
