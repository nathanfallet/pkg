package digital.guimauve.pkg.controllers.packages.npm

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.users.IGetUserForCallUseCase
import dev.kaccelero.commons.users.IRequireUserForCallUseCase
import digital.guimauve.pkg.domain.usecases.packages.IGetOrCreatePackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.IGetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.IGetPackageVersionByNameUseCase
import digital.guimauve.pkg.models.packages.PackageFormat
import digital.guimauve.pkg.models.packages.npm.NpmPackage
import digital.guimauve.pkg.models.packages.npm.NpmVersion
import digital.guimauve.pkg.models.users.User
import io.ktor.http.*
import io.ktor.server.application.*

class NpmController(
    private val getUserUseCase: IGetUserForCallUseCase,
    private val requireUserUseCase: IRequireUserForCallUseCase,
    private val getPackageUseCase: IGetPackageByNameUseCase,
    private val getOrCreatePackageUseCase: IGetOrCreatePackageUseCase,
    private val getPackageVersionUseCase: IGetPackageVersionByNameUseCase,
) : INpmController {

    override suspend fun login(call: ApplicationCall): Map<String, String> {
        return mapOf() // TODO
    }

    override suspend fun put(call: ApplicationCall, packageName: String, payload: NpmPackage) {
        val user = requireUserUseCase(call) as User
        println(payload)
    }

    override suspend fun get(call: ApplicationCall, packageName: String): NpmPackage {
        val user = getUserUseCase(call) as? User
        val `package` = getPackageUseCase(packageName, PackageFormat.NPM, user)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_not_found")
        return NpmPackage(
            id = `package`.name,
            name = `package`.name,
            description = null,
            versions = mapOf()
        )
    }

    override suspend fun getVersion(call: ApplicationCall, packageName: String, version: String): NpmVersion {
        val user = getUserUseCase(call) as? User
        val `package` = getPackageUseCase(packageName, PackageFormat.NPM, user)
            ?: throw ControllerException(HttpStatusCode.NotFound, "packages_not_found")
        val packageVersion = getPackageVersionUseCase(version, `package`.id)
            ?: throw ControllerException(HttpStatusCode.NotFound, "versions_not_found")
        return NpmVersion(
            name = `package`.name,
            version = packageVersion.version
        )
    }

}
