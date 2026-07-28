package digital.guimauve.pkg.models.packages

import digital.guimauve.zodable.Zodable
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Zodable
@JsExport
@Serializable
enum class PackageFormat {

    MAVEN, NPM, PYPI

}
