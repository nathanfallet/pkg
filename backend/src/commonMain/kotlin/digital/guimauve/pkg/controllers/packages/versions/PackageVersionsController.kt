package digital.guimauve.pkg.controllers.packages.versions

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.ListPackageVersionFilesUseCase
import digital.guimauve.pkg.models.packages.Package
import io.ktor.http.*
import io.ktor.server.application.*
import kotlin.uuid.Uuid

class PackageVersionsController(
    private val getPackageVersionUseCase: GetPackageVersionUseCase,
    private val listPackageVersionFilesUseCase: ListPackageVersionFilesUseCase,
) : IPackageVersionsController {

    override suspend fun get(call: ApplicationCall, parent: Package, id: Uuid): Map<String, Any> {
        val version = getPackageVersionUseCase(id, parent.id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "package_versions_not_found")
        val files = listPackageVersionFilesUseCase(version.id)
            .sortedBy { it.name }
        return mapOf(
            "package" to parent,
            "item" to version,
            "files" to files
        )
    }

}
