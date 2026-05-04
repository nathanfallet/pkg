package digital.guimauve.pkg.domain.usecases.packages.versions

import digital.guimauve.pkg.domain.repositories.PackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.models.users.UserContext
import kotlin.uuid.Uuid

class GetOrCreatePackageVersionUseCaseImpl(
    private val repository: PackageVersionsRepository,
) : GetOrCreatePackageVersionUseCase {
    override suspend fun invoke(name: String, packageId: Uuid, user: User): PackageVersion? =
        repository.getByName(name, packageId)
            ?: repository.create(
                CreatePackageVersionPayload(name, null),
                packageId,
                UserContext(user.id),
            )
}
