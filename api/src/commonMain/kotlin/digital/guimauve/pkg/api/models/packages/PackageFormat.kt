package digital.guimauve.pkg.api.models.packages

import dev.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * The registry a package is published to, as the API exposes it. Kept apart from the domain enum
 * of the same name so that the wire contract does not follow the domain around.
 */
@Zodable
@JsExport
@Serializable
enum class PackageFormat {

    MAVEN, NPM, PYPI

}
