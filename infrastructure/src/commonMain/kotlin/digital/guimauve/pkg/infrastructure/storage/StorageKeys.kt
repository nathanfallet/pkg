package digital.guimauve.pkg.infrastructure.storage

/**
 * Refuses a key that would resolve outside the root of the storage.
 *
 * The maven parser already rejects a traversing artifact name, but that guard is specific to one
 * registry: this is the boundary the npm and PyPI uploads will cross too once they are written.
 */
internal fun requireSafeStorageKey(path: String) {
    require(path.isNotEmpty() && !path.startsWith("/") && !path.startsWith("\\")) {
        "A storage key must be relative: $path"
    }
    require(path.split('/', '\\').none { it == "." || it == ".." }) {
        "A storage key may not traverse: $path"
    }
}
