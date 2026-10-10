package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.users.User

/**
 * Reads a package by name and format, as [user] may see it: a public package is readable by anyone,
 * anonymous included; a private one only by its own organization, and throws
 * [digital.guimauve.pkg.domain.exceptions.packages.PackagePrivateException] for anyone else.
 *
 * Returns null if there is no such package.
 */
interface GetPackageByNameUseCase {
    suspend operator fun invoke(name: String, format: PackageFormat, user: User?): Package?
}
