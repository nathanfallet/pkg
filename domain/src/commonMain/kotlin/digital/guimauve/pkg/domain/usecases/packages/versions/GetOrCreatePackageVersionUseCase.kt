package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

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
