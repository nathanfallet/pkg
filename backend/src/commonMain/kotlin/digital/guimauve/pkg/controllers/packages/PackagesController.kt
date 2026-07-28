package digital.guimauve.pkg.controllers.packages

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.usecases.packages.GetPackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.ListPackagesUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.ListPackageVersionsUseCase
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.packages.Package
import io.ktor.http.*
import io.ktor.server.application.*
import kotlin.uuid.Uuid

class PackagesController(
    private val listPackagesUseCase: ListPackagesUseCase,
    private val getPackageUseCase: GetPackageUseCase,
    private val listPackageVersionsUseCase: ListPackageVersionsUseCase,
) : IPackagesController {

    override suspend fun list(call: ApplicationCall, parent: Organization): List<Package> {
        return listPackagesUseCase(parent.id)
    }

    override suspend fun get(call: ApplicationCall, parent: Organization, id: Uuid): Map<String, Any> {
        val pkg = getPackageUseCase(id, parent.id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_not_found")
        val versions = listPackageVersionsUseCase(pkg.id)
            .sortedByDescending { it.publishedAt }
        return mapOf(
            "item" to pkg,
            "versions" to versions
        )
    }

}
