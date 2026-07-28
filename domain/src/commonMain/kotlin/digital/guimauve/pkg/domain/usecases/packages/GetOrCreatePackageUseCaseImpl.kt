package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.exceptions.packages.PackageWriteForbiddenException
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User

class GetOrCreatePackageUseCaseImpl(
    private val repository: PackagesRepository,
) : GetOrCreatePackageUseCase {
    override suspend fun invoke(name: String, format: PackageFormat, user: User): Package? {
        repository.getByName(name, format)?.let { existingPackage ->
            if (existingPackage.organizationId != user.organizationId) throw PackageWriteForbiddenException()
            return existingPackage
        }

        return repository.create(
            CreatePackagePayload(
                name = name,
                format = format,
                isPublic = false, // TODO: Make this configurable
            ),
            user.organizationId
        )
    }
}
