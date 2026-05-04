package digital.guimauve.pkg.controllers.packages.pypi

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.users.IGetUserForCallUseCase
import dev.kaccelero.commons.users.IRequireUserForCallUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*

class PyPiController(
    private val getUserUseCase: IGetUserForCallUseCase,
    private val requireUserUseCase: IRequireUserForCallUseCase,
    private val getPackageUseCase: GetPackageByNameUseCase,
    private val getOrCreatePackageUseCase: GetOrCreatePackageUseCase,
) : IPyPiController {

    override suspend fun root(): Map<String, Any> {

        return mapOf(
            "packages" to listOf<Package>()
        )
    }

    override suspend fun packageInfo(call: ApplicationCall, packageName: String): Map<String, Any> {
        val user = getUserUseCase(call) as? User
        val `package` = getPackageUseCase(packageName, PackageFormat.PYPI, user)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_not_found")
        return mapOf(
            "package" to `package`,
            "files" to listOf<PackageVersionFile>()
        )
    }

    override suspend fun upload(call: ApplicationCall) {
        val user = requireUserUseCase(call) as User
        val t = call.receiveMultipart()
        t.forEachPart { part ->
            println(part.name)
        }
    }

}
