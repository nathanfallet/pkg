package digital.guimauve.pkg.presentation.routes.dashboard

import digital.guimauve.pkg.domain.exceptions.organizations.OrganizationNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import digital.guimauve.pkg.domain.exceptions.packages.versions.PackageVersionNotFoundException
import digital.guimauve.pkg.domain.exceptions.users.UserNotFoundException
import digital.guimauve.pkg.domain.usecases.organizations.GetOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.packages.GetPackageUseCase
import digital.guimauve.pkg.domain.usecases.packages.ListPackagesUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.GetPackageVersionUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.ListPackageVersionsUseCase
import digital.guimauve.pkg.domain.usecases.packages.versions.files.ListPackageVersionFilesUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserInOrganizationUseCase
import digital.guimauve.pkg.domain.usecases.users.GetUserUseCase
import digital.guimauve.pkg.domain.usecases.users.ListUsersUseCase
import digital.guimauve.pkg.models.organizations.Organization
import digital.guimauve.pkg.models.users.User
import digital.guimauve.pkg.presentation.extensions.respondView
import digital.guimauve.pkg.presentation.extensions.userOrNull
import digital.guimauve.pkg.presentation.mappers.organizations.toOrganizationView
import digital.guimauve.pkg.presentation.mappers.packages.toPackageView
import digital.guimauve.pkg.presentation.mappers.packages.versions.files.toPackageVersionFileView
import digital.guimauve.pkg.presentation.mappers.packages.versions.toPackageVersionView
import digital.guimauve.pkg.presentation.mappers.users.toUserView
import digital.guimauve.pkg.presentation.views.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.uuid.Uuid

/**
 * Dependencies required for the dashboard routes.
 */
data class DashboardRoutesDependencies(
    val listPackagesUseCase: ListPackagesUseCase,
    val getPackageUseCase: GetPackageUseCase,
    val listPackageVersionsUseCase: ListPackageVersionsUseCase,
    val getPackageVersionUseCase: GetPackageVersionUseCase,
    val listPackageVersionFilesUseCase: ListPackageVersionFilesUseCase,
    val listUsersUseCase: ListUsersUseCase,
    val getUserInOrganizationUseCase: GetUserInOrganizationUseCase,
    val getOrganizationUseCase: GetOrganizationUseCase,
    val getUserUseCase: GetUserUseCase,
)

/**
 * The signed in visitor and the organization it belongs to, which every dashboard page needs.
 */
private data class DashboardContext(
    val user: User,
    val organization: Organization,
)

/**
 * Resolves the visitor of a dashboard page. An anonymous one is sent to the login page rather than
 * being answered a 401, so this returns null once the redirect has been written.
 */
private suspend fun RoutingContext.dashboardContext(
    dependencies: DashboardRoutesDependencies,
): DashboardContext? = with(dependencies) {
    val user = call.userOrNull(getUserUseCase) ?: run {
        call.respondRedirect("/auth/login?redirect=${call.request.uri}")
        return null
    }
    val organization = getOrganizationUseCase(user.organizationId) ?: throw OrganizationNotFoundException()
    return DashboardContext(user, organization)
}

/**
 * Reads a path parameter holding an identifier. A malformed one simply means no such resource.
 */
private fun RoutingContext.uuidParameter(name: String): Uuid? =
    call.parameters[name]?.let { runCatching { Uuid.parse(it) }.getOrNull() }

/**
 * Builds the layout every dashboard page is wrapped in.
 */
private fun DashboardContext.layout(title: String) = LayoutView(title, user.toUserView())

/**
 * Configures the dashboard routes.
 */
fun Route.dashboardRoutes(dependencies: DashboardRoutesDependencies) = with(dependencies) {
    get("/") {
        call.respondRedirect("/packages")
    }

    get("/packages") {
        val context = dashboardContext(dependencies) ?: return@get
        val packages = listPackagesUseCase(context.organization.id).map { it.toPackageView() }
        call.respondView("public/packages/list.ftl", PackagesPageView(context.layout("packages_title"), packages))
    }

    get("/packages/{packageId}") {
        val context = dashboardContext(dependencies) ?: return@get
        val packageId = uuidParameter("packageId") ?: throw PackageNotFoundException()
        val pkg = getPackageUseCase(packageId, context.organization.id) ?: throw PackageNotFoundException()
        val versions = listPackageVersionsUseCase(pkg.id)
            .sortedByDescending { it.publishedAt }
            .map { it.toPackageVersionView() }
        call.respondView(
            "public/packages/detail.ftl",
            PackagePageView(context.layout("packages_title"), pkg.toPackageView(), versions)
        )
    }

    get("/packages/{packageId}/versions/{versionId}") {
        val context = dashboardContext(dependencies) ?: return@get
        val packageId = uuidParameter("packageId") ?: throw PackageNotFoundException()
        val versionId = uuidParameter("versionId") ?: throw PackageVersionNotFoundException()
        val pkg = getPackageUseCase(packageId, context.organization.id) ?: throw PackageNotFoundException()
        val version = getPackageVersionUseCase(versionId, pkg.id) ?: throw PackageVersionNotFoundException()
        val files = listPackageVersionFilesUseCase(version.id)
            .sortedBy { it.name }
            .map { it.toPackageVersionFileView() }
        call.respondView(
            "public/packages/versions/detail.ftl",
            PackageVersionPageView(
                context.layout("packages_title"),
                version.toPackageVersionView(),
                pkg.toPackageView(),
                files
            )
        )
    }

    get("/users") {
        val context = dashboardContext(dependencies) ?: return@get
        val users = listUsersUseCase(context.organization.id).map { it.toUserView() }
        call.respondView("public/users/list.ftl", UsersPageView(context.layout("users_title"), users))
    }

    get("/users/{userId}") {
        val context = dashboardContext(dependencies) ?: return@get
        val userId = uuidParameter("userId") ?: throw UserNotFoundException()
        val user = getUserInOrganizationUseCase(userId, context.organization.id) ?: throw UserNotFoundException()
        call.respondView(
            "public/users/detail.ftl",
            UserPageView(context.layout("users_title"), user.toUserView(), context.organization.toOrganizationView())
        )
    }
}
