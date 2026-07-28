package digital.guimauve.pkg.presentation.routes.packages.pypi

import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.usecases.packages.GetPackageByNameUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.presentation.extensions.requireUser
import digital.guimauve.pkg.presentation.extensions.respondView
import digital.guimauve.pkg.presentation.extensions.userOrNull
import digital.guimauve.pkg.presentation.views.PyPiPackagePageView
import digital.guimauve.pkg.presentation.views.PyPiRootPageView
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

// Spec: https://peps.python.org/pep-0503/

/**
 * Dependencies required for the PyPI registry routes.
 */
data class PyPiRoutesDependencies(
    val getPackageByNameUseCase: GetPackageByNameUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * Configures the PyPI registry routes.
 */
fun Route.pypiRoutes(dependencies: PyPiRoutesDependencies) = with(dependencies) {
    get("/pypi/simple") {
        // TODO: list the PyPI packages of the organization the call is authenticated for
        call.respondView("pypi/root.ftl", PyPiRootPageView(packages = emptyList()))
    }

    get("/pypi/simple/{packageName}") {
        val user = call.userOrNull(getUserUseCase)
        val packageName = call.parameters["packageName"].orEmpty()
        val pkg = getPackageByNameUseCase(packageName, PackageFormat.PYPI, user)
            ?: throw PackageNotFoundException()
        // TODO: list the files of every version of the package
        call.respondView("pypi/package.ftl", PyPiPackagePageView(pkg.name, files = emptyList()))
    }

    post("/pypi") {
        call.requireUser(getUserUseCase)
        // TODO: store the uploaded distribution
        call.receiveMultipart().forEachPart { it.dispose() }
        call.respond(HttpStatusCode.NoContent)
    }
}
