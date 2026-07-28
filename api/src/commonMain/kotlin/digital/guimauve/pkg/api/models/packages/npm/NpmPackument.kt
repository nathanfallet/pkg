package digital.guimauve.pkg.api.models.packages.npm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The document `npm` reads to resolve a package, known as a packument.
 *
 * A version manifest carries far more than this registry understands — `dependencies`, `bin`,
 * `engines`, `peerDependenciesMeta`, and whatever a future npm adds — so it is kept as raw json and
 * handed back untouched apart from the tarball url, which only the registry can know.
 */
@Serializable
data class NpmPackument(
    @SerialName("_id") val id: String,
    val name: String,
    @SerialName("dist-tags") val distTags: Map<String, String>,
    val versions: Map<String, JsonObject>,
)
