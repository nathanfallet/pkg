package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.domain.models.packages.Package
import kotlin.uuid.Uuid

interface ListPackagesUseCase {
    suspend operator fun invoke(organizationId: Uuid): List<Package>
}
