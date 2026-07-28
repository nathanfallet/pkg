package digital.guimauve.pkg.presentation.routes.packages.npm

import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException

/**
 * What the segments after `/npm/` point at.
 *
 * The client does not spell a package name the same way twice: it percent-encodes the scope
 * separator when it asks for a document (`@scope%2fname`, one segment) but not when it follows a
 * tarball url (`@scope/name/-/file.tgz`, four segments). Both have to resolve to the same package.
 */
sealed interface NpmPath {

    val packageName: String

    /** The whole document of a package, which is what resolution reads. */
    data class Packument(override val packageName: String) : NpmPath

    /** A single version of a package. */
    data class Version(override val packageName: String, val version: String) : NpmPath

    /** The archive itself. */
    data class Tarball(override val packageName: String, val fileName: String) : NpmPath

}

/**
 * Reads the segments after `/npm/`.
 *
 * @throws PackageNotFoundException if they name nothing.
 */
fun parseNpmPath(segments: List<String>): NpmPath {
    if (segments.isEmpty()) throw PackageNotFoundException()

    // `.../-/file.tgz`, the only shape carrying a marker segment.
    val marker = segments.indexOf("-")
    if (marker > 0 && marker == segments.size - 2) {
        return NpmPath.Tarball(segments.take(marker).joinToString("/"), segments.last())
    }

    // A scope always leads, and always comes with the name it scopes.
    val nameLength = if (segments.first().startsWith("@") && segments.size > 1) 2 else 1
    val name = segments.take(nameLength).joinToString("/")
    val rest = segments.drop(nameLength)
    return when {
        rest.isEmpty() -> NpmPath.Packument(name)
        rest.size == 1 -> NpmPath.Version(name, rest.single())
        else -> throw PackageNotFoundException()
    }
}
