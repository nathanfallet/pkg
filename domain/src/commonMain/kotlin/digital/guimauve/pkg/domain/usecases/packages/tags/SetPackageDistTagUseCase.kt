package digital.guimauve.pkg.domain.usecases.packages.tags

import kotlin.uuid.Uuid

/**
 * Points a dist-tag of a package at a version, creating the tag or moving it: what a publication
 * does to the tag it was published under (`latest` unless `npm publish --tag` says otherwise).
 */
interface SetPackageDistTagUseCase {
    suspend operator fun invoke(packageId: Uuid, tag: String, versionId: Uuid)
}
