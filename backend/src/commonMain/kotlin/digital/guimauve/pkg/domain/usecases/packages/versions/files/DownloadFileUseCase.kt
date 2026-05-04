package digital.guimauve.pkg.domain.usecases.packages.versions.files

import dev.kaccelero.commons.responses.BytesResponse
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile

interface DownloadFileUseCase {
    suspend operator fun invoke(input: PackageVersionFile): BytesResponse
}
