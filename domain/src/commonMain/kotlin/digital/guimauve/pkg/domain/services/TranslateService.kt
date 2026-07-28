package digital.guimauve.pkg.domain.services

/**
 * Interface for translating the keys of the message bundle.
 */
interface TranslateService {

    /**
     * Translates a key.
     *
     * @param key The key to translate.
     * @param language The language tag to translate it into, e.g. `en`.
     *
     * @return The translation, or the key itself when there is none.
     */
    fun translate(key: String, language: String): String

}
