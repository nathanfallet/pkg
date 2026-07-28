package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User

interface GetPackageByNameUseCase {
    suspend operator fun invoke(name: String, format: PackageFormat, user: User?): Package?
}
