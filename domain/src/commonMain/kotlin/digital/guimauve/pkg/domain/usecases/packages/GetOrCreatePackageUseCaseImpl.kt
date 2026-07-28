package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.exceptions.packages.PackageWriteForbiddenException
import digital.guimauve.pkg.domain.models.packages.CreatePackagePayload
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.PackagesRepository

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
