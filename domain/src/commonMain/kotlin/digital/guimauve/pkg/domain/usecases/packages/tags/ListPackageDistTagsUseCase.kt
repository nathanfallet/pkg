package digital.guimauve.pkg.domain.usecases.packages.tags

import kotlin.uuid.Uuid

/** Lists the dist-tags of a package, each with the identifier of the version it points to. */
interface ListPackageDistTagsUseCase {
    suspend operator fun invoke(packageId: Uuid): Map<String, Uuid>
}
