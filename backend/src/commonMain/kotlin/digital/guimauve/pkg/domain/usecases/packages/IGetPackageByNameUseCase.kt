package digital.guimauve.pkg.domain.usecases.packages

import dev.kaccelero.usecases.ITripleSuspendUseCase
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.users.User

interface IGetPackageByNameUseCase : ITripleSuspendUseCase<String, PackageFormat, User?, Package?>
