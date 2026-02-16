package digital.guimauve.pkg.controllers.packages.versions

import dev.kaccelero.annotations.*
import dev.kaccelero.controllers.IChildModelController
import digital.guimauve.pkg.models.packages.Package
import digital.guimauve.pkg.models.packages.versions.CreatePackageVersionPayload
import digital.guimauve.pkg.models.packages.versions.PackageVersion
import io.ktor.server.application.*
import kotlin.uuid.Uuid

interface IPackageVersionsController :
    IChildModelController<PackageVersion, Uuid, CreatePackageVersionPayload, Unit, Package, Uuid> {

    @APIMapping
    @TemplateMapping("public/packages/versions/detail.ftl")
    @GetModelPath
    @DocumentedError(404, "package_versions_not_found")
    suspend fun get(call: ApplicationCall, @ParentModel parent: Package, @Id id: Uuid): Map<String, Any>

}
