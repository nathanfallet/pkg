package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import digital.guimauve.pkg.domain.models.users.User
import kotlin.uuid.Uuid

interface GetOrCreatePackageVersionUseCase {
    suspend operator fun invoke(name: String, packageId: Uuid, user: User): PackageVersion?
}
