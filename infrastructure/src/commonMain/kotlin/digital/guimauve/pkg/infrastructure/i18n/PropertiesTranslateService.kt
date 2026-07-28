package digital.guimauve.pkg.infrastructure.i18n

import digital.guimauve.pkg.domain.services.TranslateService
import java.util.*

/**
 * Implementation of [TranslateService] reading the `i18n/Messages_*.properties` bundles.
 */
class PropertiesTranslateService(
    private val baseName: String = DEFAULT_BUNDLE,
) : TranslateService {

    companion object {

        const val DEFAULT_BUNDLE = "i18n.Messages"
        const val DEFAULT_LANGUAGE = "en"

    }

    // A missing translation must not take a page down: fall back to the default language, then to
    // the key itself, which is readable enough.
    override fun translate(key: String, language: String): String =
        lookup(key, language) ?: lookup(key, DEFAULT_LANGUAGE) ?: key

    private fun lookup(key: String, language: String): String? = try {
        ResourceBundle.getBundle(baseName, Locale.forLanguageTag(language)).getString(key)
    } catch (exception: MissingResourceException) {
        null
    }

}
