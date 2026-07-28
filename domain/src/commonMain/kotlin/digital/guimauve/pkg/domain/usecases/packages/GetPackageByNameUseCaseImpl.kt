package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.exceptions.packages.PackagePrivateException
import digital.guimauve.pkg.domain.repositories.PackagesRepository
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User

class GetPackageByNameUseCaseImpl(
    private val repository: PackagesRepository,
) : GetPackageByNameUseCase {
    override suspend fun invoke(name: String, format: PackageFormat, user: User?): Package? {
        val pkg = repository.getByName(name, format) ?: return null
        if (!pkg.isPublic && pkg.organizationId != user?.organizationId) throw PackagePrivateException()
        return pkg
    }
}
