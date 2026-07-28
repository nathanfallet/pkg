package digital.guimauve.pkg.domain.usecases.packages

import digital.guimauve.pkg.models.packages.Package
import kotlin.uuid.Uuid

interface GetPackageUseCase {
    suspend operator fun invoke(packageId: Uuid, organizationId: Uuid): Package?
}
