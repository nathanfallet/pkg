package digital.guimauve.pkg.infrastructure.database

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class H2DatabaseFactoryTest {

    @Test
    fun aFileDatabaseKeepsItsDataWhenReopened() {
        val directory = Files.createTempDirectory("pkg-h2").toFile()
        try {
            val config = DatabaseConfig(protocol = "h2", name = "pkg", directory = directory.absolutePath)

            val first = H2DatabaseFactory(config)
            first.migrate()
            transaction(first.getDatabase()) {
                exec("CREATE TABLE probe (id INT)")
                exec("INSERT INTO probe (id) VALUES (42)")
            }

            // A new factory on the same directory, like the next boot of a local run.
            val second = H2DatabaseFactory(config)
            second.migrate()
            val id = transaction(second.getDatabase()) {
                exec("SELECT id FROM probe") { rs -> rs.next(); rs.getInt(1) }
            }

            assertEquals(42, id)
            assertTrue(directory.resolve("pkg.mv.db").exists(), "the database is written in the directory")
        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun normalizeDirectoryPrefixesABareRelativePath() {
        assertEquals("./data", H2DatabaseFactory.normalizeDirectory("data"))
        assertEquals("./data", H2DatabaseFactory.normalizeDirectory("data/"))
    }

    @Test
    fun normalizeDirectoryKeepsPathsH2Accepts() {
        assertEquals("./data", H2DatabaseFactory.normalizeDirectory("./data/"))
        assertEquals("/var/lib/pkg", H2DatabaseFactory.normalizeDirectory("/var/lib/pkg"))
        assertEquals("~/pkg", H2DatabaseFactory.normalizeDirectory("~/pkg"))
    }

}
