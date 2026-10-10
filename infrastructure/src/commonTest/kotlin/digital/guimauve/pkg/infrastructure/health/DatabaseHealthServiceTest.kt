package digital.guimauve.pkg.infrastructure.health

import digital.guimauve.pkg.infrastructure.database.DatabaseFactory
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseHealthServiceTest {

    private fun database(healthy: Boolean) = mockk<DatabaseFactory> { every { isHealthy() } returns healthy }

    @Test
    fun `a reachable database is reported up`() = runBlocking {
        assertTrue(DatabaseHealthService(database(healthy = true)).isDatabaseHealthy())
    }

    @Test
    fun `a closed pool is reported down`() = runBlocking {
        assertFalse(DatabaseHealthService(database(healthy = false)).isDatabaseHealthy())
    }

}
