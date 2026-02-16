package digital.guimauve.pkg.domain.usecases.packages

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.models.packages.CreatePackagePayload
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*

class GetOrCreatePackageUseCase(
    private val repository: PackagesRepository,
) : IGetOrCreatePackageUseCase {

    override suspend fun invoke(input1: String, input2: PackageFormat, input3: User): Package? {
        repository.getByName(input1, input2)?.let { existingPackage ->
            if (existingPackage.organizationId != input3.organizationId)
                throw ControllerException(HttpStatusCode.Forbidden, "packages_write_forbidden")
            return existingPackage
        }

        return repository.create(
            CreatePackagePayload(
                name = input1,
                format = input2,
                isPublic = false, // TODO: Make this configurable
            ),
            input3.organizationId
        )
    }

}
