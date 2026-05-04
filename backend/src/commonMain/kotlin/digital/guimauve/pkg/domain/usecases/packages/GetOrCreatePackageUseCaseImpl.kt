package digital.guimauve.pkg.domain.usecases.packages

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*

class GetOrCreatePackageUseCaseImpl(
    private val repository: PackagesRepository,
) : GetOrCreatePackageUseCase {
    override suspend fun invoke(name: String, format: PackageFormat, user: User): Package? {
        repository.getByName(name, format)?.let { existingPackage ->
            if (existingPackage.organizationId != user.organizationId)
                throw ControllerException(HttpStatusCode.Forbidden, "packages_write_forbidden")
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
