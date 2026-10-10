package digital.guimauve.pkg.infrastructure.bcrypt

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BCryptPasswordEncoderServiceTest {

    private val encoder = BCryptPasswordEncoderService()

    @Test
    fun `a password matches its own hash only`() {
        val hash = encoder.encode("secret")

        assertNotEquals("secret", hash, "the password is never stored as typed")
        assertTrue(encoder.matches("secret", hash))
        assertFalse(encoder.matches("Secret", hash))
        assertFalse(encoder.matches("", hash))
    }

    @Test
    fun `the same password hashes differently every time`() {
        // A salt per hash: two users with the same password do not share a hash.
        val first = encoder.encode("secret")
        val second = encoder.encode("secret")

        assertNotEquals(first, second)
        assertTrue(encoder.matches("secret", first))
        assertTrue(encoder.matches("secret", second))
    }

}
