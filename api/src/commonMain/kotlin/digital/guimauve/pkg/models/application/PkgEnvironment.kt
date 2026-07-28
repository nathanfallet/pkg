package digital.guimauve.pkg.models.application

import digital.guimauve.zodable.Zodable
import kotlin.js.JsExport

@Zodable
@JsExport
enum class PkgEnvironment {

    PRODUCTION, DEV;

    val baseUrl: String
        get() = when (this) {
            PRODUCTION -> "https://pkg.guimauve.digital"
            DEV -> "http://localhost:8080"
        }

}
