package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.users.User

interface GetPackageByNameUseCase {
    suspend operator fun invoke(name: String, format: PackageFormat, user: User?): Package?
}
