package digital.guimauve.pkg.presentation.config

import dev.kaccelero.plugins.I18n
import io.ktor.server.application.*
import java.util.*

fun Application.configureI18n() {
    install(I18n) {
        supportedLocales = listOf("en", "fr").map(Locale::forLanguageTag)
        useOfUri = true
    }
}
