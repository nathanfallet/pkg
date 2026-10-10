package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.users.User

/**
 * Returns the package a user publishes to, registering it for the user's organization the first time
 * it is seen. A new package is private. A package owned by another organization throws
 * [digital.guimauve.pkg.domain.exceptions.packages.PackageWriteForbiddenException].
 *
 * Returns null if the package could not be created.
 */
interface GetOrCreatePackageUseCase {
    suspend operator fun invoke(name: String, format: PackageFormat, user: User): Package?
}
