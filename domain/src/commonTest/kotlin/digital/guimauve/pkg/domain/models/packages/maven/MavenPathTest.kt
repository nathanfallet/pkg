package digital.guimauve.pkg.domain.models.packages.maven

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Only the module metadata and its checksums are rewritable: never a file of a version. */
class MavenPathTest {

    private fun path(version: String?, filename: String) = MavenPath("com.example", "library", version, filename)

    @Test
    fun theModuleMetadataAndItsChecksumsAreModuleMetadata() {
        listOf("", ".md5", ".sha1", ".sha256", ".sha512").forEach {
            assertTrue(path(null, "maven-metadata.xml$it").isModuleMetadata, "maven-metadata.xml$it")
        }
    }

    @Test
    fun theMetadataOfAVersionIsNot() {
        assertFalse(path("1.0.0-SNAPSHOT", "maven-metadata.xml").isModuleMetadata)
    }

    @Test
    fun anyOtherFileWithoutAVersionIsNot() {
        assertFalse(path(null, "library-1.0.0.jar").isModuleMetadata)
        assertFalse(path(null, "maven-metadata.xmlx").isModuleMetadata)
    }

}
