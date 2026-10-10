package digital.guimauve.pkg.domain.usecases.users

import digital.guimauve.pkg.domain.models.users.CreateUserPayload
import digital.guimauve.pkg.domain.models.users.User
import digital.guimauve.pkg.domain.repositories.UsersRepository
import digital.guimauve.pkg.domain.services.PasswordEncoderService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/** Locks that a password reaches the repository hashed, never as typed. */
class CreateUserUseCaseTest {

    private val organizationId = Uuid.random()

    private val repository = mockk<UsersRepository>()
    private val encoder = mockk<PasswordEncoderService> { every { encode("secret") } returns "hashed-secret" }

    private val useCase = CreateUserUseCaseImpl(repository, encoder)

    @Test
    fun `the password is stored hashed, the email as given`() = runBlocking {
        val created = User(Uuid.random(), organizationId, "john@example.com", null)
        coEvery { repository.create(any(), any()) } returns created

        assertEquals(created, useCase(CreateUserPayload("john@example.com", "secret"), organizationId))

        coVerify(exactly = 1) {
            repository.create(CreateUserPayload("john@example.com", "hashed-secret"), organizationId)
        }
    }

    @Test
    fun `a user the repository could not create is reported as null`() = runBlocking {
        coEvery { repository.create(any(), any()) } returns null

        assertNull(useCase(CreateUserPayload("john@example.com", "secret"), organizationId))
    }

}
