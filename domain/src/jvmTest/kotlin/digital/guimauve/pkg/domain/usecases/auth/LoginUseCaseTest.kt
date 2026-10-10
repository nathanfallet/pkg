package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.domain.models.auth.LoginPayload
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import digital.guimauve.pkg.domain.usecases.users.GetUserForEmailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/**
 * Locks what a login answers, and that an unknown address costs a hash comparison like a known
 * one: otherwise the time to answer tells which addresses are registered.
 */
class LoginUseCaseTest {

    private val user = User(Uuid.random(), Uuid.random(), "john@example.com", "hash")

    private val getUserForEmail = mockk<GetUserForEmailUseCase>()
    private val encoder = mockk<PasswordEncoderService> {
        every { encode(any()) } returns "dummy-hash"
        every { matches(any(), any()) } returns false
        every { matches("secret", "hash") } returns true
    }

    private val useCase = LoginUseCaseImpl(getUserForEmail, encoder)

    @Test
    fun `the right password returns the user`() = runBlocking {
        coEvery { getUserForEmail("john@example.com", true) } returns user

        assertEquals(user, useCase(LoginPayload("john@example.com", "secret")))
    }

    @Test
    fun `a wrong password returns nothing`() = runBlocking {
        coEvery { getUserForEmail("john@example.com", true) } returns user

        assertNull(useCase(LoginPayload("john@example.com", "wrong")))
    }

    @Test
    fun `the user is read with its password, which is what gets compared`() = runBlocking {
        coEvery { getUserForEmail(any(), any()) } returns user

        useCase(LoginPayload("john@example.com", "secret"))

        coVerify(exactly = 1) { getUserForEmail("john@example.com", true) }
    }

    @Test
    fun `an unknown address still compares a hash before answering nothing`() = runBlocking {
        coEvery { getUserForEmail("ghost@example.com", true) } returns null

        assertNull(useCase(LoginPayload("ghost@example.com", "secret")))

        verify(exactly = 1) { encoder.matches("secret", "dummy-hash") }
    }

    @Test
    fun `an account without a password is refused like an unknown one`() = runBlocking {
        coEvery { getUserForEmail("john@example.com", true) } returns user.copy(password = null)

        assertNull(useCase(LoginPayload("john@example.com", "secret")))

        verify(exactly = 1) { encoder.matches("secret", "dummy-hash") }
    }

    @Test
    fun `the dummy hash is computed once, on the first miss`() = runBlocking {
        coEvery { getUserForEmail(any(), true) } returns null

        useCase(LoginPayload("ghost@example.com", "secret"))
        useCase(LoginPayload("other@example.com", "secret"))

        verify(exactly = 1) { encoder.encode(any()) }
        verify(exactly = 2) { encoder.matches("secret", "dummy-hash") }
    }

    @Test
    fun `a known address never computes the dummy hash`() = runBlocking {
        coEvery { getUserForEmail("john@example.com", true) } returns user

        useCase(LoginPayload("john@example.com", "secret"))

        verify(exactly = 0) { encoder.encode(any()) }
    }

}
