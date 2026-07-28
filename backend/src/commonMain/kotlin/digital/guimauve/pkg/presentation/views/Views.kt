package digital.guimauve.pkg.presentation.views

/**
 * Everything below is what the templates see. Freemarker is happier with plain types than with
 * domain objects, and it keeps the templates free of any logic.
 */

data class LayoutView(
    val title: String,
    /**
     * The signed in user, or null for an anonymous visitor.
     */
    val user: UserView?,
)

data class OrganizationView(
    val id: String,
    val name: String,
)

data class UserView(
    val id: String,
    val email: String,
    val organizationId: String,
    val url: String,
)

data class PackageView(
    val id: String,
    val name: String,
    val format: String,
    val createdAt: String,
    val url: String,
    /**
     * "Public" or "Private", already worded for the badge.
     */
    val visibility: String,
    /**
     * The bootstrap class colouring the visibility badge.
     */
    val visibilityBadge: String,
)

data class PackageVersionView(
    val id: String,
    val version: String,
    val publishedAt: String,
    val metadata: String?,
    val yanked: Boolean,
    val url: String,
)

data class PackageVersionFileView(
    val name: String,
    val contentType: String,
    /**
     * The size, already formatted, e.g. "12.3 KB".
     */
    val size: String,
    val url: String,
)

data class LoginPageView(
    val layout: LayoutView,
    /**
     * The message key to display, when a sign in attempt just failed.
     */
    val error: String?,
)

data class PackagesPageView(
    val layout: LayoutView,
    val packages: List<PackageView>,
)

data class PackagePageView(
    val layout: LayoutView,
    val item: PackageView,
    val versions: List<PackageVersionView>,
)

data class PackageVersionPageView(
    val layout: LayoutView,
    val item: PackageVersionView,
    val packageItem: PackageView,
    val files: List<PackageVersionFileView>,
)

data class UsersPageView(
    val layout: LayoutView,
    val users: List<UserView>,
)

data class UserPageView(
    val layout: LayoutView,
    val item: UserView,
    val organization: OrganizationView,
)

data class ErrorPageView(
    val layout: LayoutView,
    val status: String,
    val message: String,
)

/**
 * The two pages below are not part of the dashboard: they are the simple repository index pip
 * reads, described by PEP 503.
 */
data class PyPiLinkView(
    val name: String,
    val url: String,
)

data class PyPiRootPageView(
    val packages: List<PyPiLinkView>,
)

data class PyPiPackagePageView(
    val name: String,
    val files: List<PyPiLinkView>,
)
