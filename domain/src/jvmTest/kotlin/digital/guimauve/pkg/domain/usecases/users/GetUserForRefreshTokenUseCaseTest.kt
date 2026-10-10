package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.services.TokenService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/** Locks which user a refresh token resolves to, and that an invalid one resolves to nobody. */
class GetUserForRefreshTokenUseCaseTest {

    private val user = User(Uuid.random(), Uuid.random(), "john@example.com", null)

    private val tokenService = mockk<TokenService>()
    private val getUser = mockk<GetUserUseCase>()

    private val useCase = GetUserForRefreshTokenUseCaseImpl(tokenService, getUser)

    @Test
    fun `a valid token resolves to the user it names`() = runBlocking {
        every { tokenService.verifyToken("token") } returns user.id
        coEvery { getUser(user.id) } returns user

        assertEquals(user, useCase("token"))
    }

    @Test
    fun `an invalid token resolves to nobody, without reading any user`() = runBlocking {
        every { tokenService.verifyToken("forged") } returns null

        assertNull(useCase("forged"))

        coVerify(exactly = 0) { getUser(any()) }
    }

    @Test
    fun `a valid token for a user who no longer exists resolves to nobody`() = runBlocking {
        every { tokenService.verifyToken("token") } returns user.id
        coEvery { getUser(user.id) } returns null

        assertNull(useCase("token"))
    }

}
