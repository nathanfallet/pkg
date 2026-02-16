package digital.guimauve.pkg.domain.usecases.packages

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*

class GetPackageByNameUseCase(
    private val repository: PackagesRepository,
) : IGetPackageByNameUseCase {

    override suspend fun invoke(input1: String, input2: PackageFormat, input3: User?): Package? {
        val pkg = repository.getByName(input1, input2) ?: return null
        if (!pkg.isPublic && pkg.organizationId != input3?.organizationId)
            throw ControllerException(HttpStatusCode.Unauthorized, "packages_private")
        return pkg
    }

}
