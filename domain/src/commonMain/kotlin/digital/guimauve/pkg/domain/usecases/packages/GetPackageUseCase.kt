package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import kotlin.uuid.Uuid

/** Reads a package of an organization, or null if that organization has no such package. */
interface GetPackageUseCase {
    suspend operator fun invoke(packageId: Uuid, organizationId: Uuid): Package?
}
