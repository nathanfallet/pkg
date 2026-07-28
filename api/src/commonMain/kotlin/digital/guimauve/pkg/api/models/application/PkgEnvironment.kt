package digital.guimauve.pkg.api.models.application

import dev.zodable.Zodable
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
