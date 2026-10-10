package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

/**
 * Returns a package's version by its version string, recording it as published by [User] the first
 * time it is seen. An existing version is returned as is: its metadata is not replaced. It does not
 * check that the user may write to the package; that is the caller's job.
 *
 * Returns null if the version could not be created.
 */
interface GetOrCreatePackageVersionUseCase {

    /**
     * @param metadata What the registry needs to hand back verbatim when the version is resolved,
     * such as an npm version manifest. Maven has nothing of the sort and passes null.
     */
    suspend operator fun invoke(
        name: String,
        packageId: Uuid,
        user: User,
        metadata: String? = null,
    ): PackageVersion?

}
