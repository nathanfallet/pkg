package digital.guimauve.pkg.infrastructure.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

/** A missing translation must never take a page down: it falls back to English, then to the key. */
class PropertiesTranslateServiceTest {

    private val service = PropertiesTranslateService()

    @Test
    fun `a key is translated from the bundle`() {
        assertEquals("Sign in", service.translate("auth_login_title", "en"))
    }

    @Test
    fun `a language without a bundle falls back to english`() {
        assertEquals("Sign in", service.translate("auth_login_title", "xx"))
    }

    @Test
    fun `a key missing from every bundle is returned as is`() {
        assertEquals("no_such_key", service.translate("no_such_key", "en"))
    }

    @Test
    fun `a bundle that does not exist falls back to the key`() {
        assertEquals("auth_login_title", PropertiesTranslateService("i18n.Missing").translate("auth_login_title", "en"))
    }

}
