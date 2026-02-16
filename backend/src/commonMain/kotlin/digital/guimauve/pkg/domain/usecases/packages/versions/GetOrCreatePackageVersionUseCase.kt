package digital.guimauve.pkg.domain.usecases.packages.versions

import dev.kaccelero.models.UUID
import digital.guimauve.pkg.domain.repositories.IPackageVersionsRepository
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.models.users.UserContext

class GetOrCreatePackageVersionUseCase(
    private val repository: IPackageVersionsRepository,
) : IGetOrCreatePackageVersionUseCase {

    override suspend fun invoke(input1: String, input2: UUID, input3: User): PackageVersion? =
        repository.getByName(input1, input2)
            ?: repository.create(
                CreatePackageVersionPayload(input1, null),
                input2,
                UserContext(input3.id),
            )

}
