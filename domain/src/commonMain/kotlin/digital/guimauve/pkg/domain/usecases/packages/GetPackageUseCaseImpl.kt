package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import kotlin.uuid.Uuid

class GetPackageUseCaseImpl(
    private val repository: PackagesRepository,
) : GetPackageUseCase {
    override suspend fun invoke(packageId: Uuid, organizationId: Uuid): Package? =
        repository.get(packageId, organizationId)
}
