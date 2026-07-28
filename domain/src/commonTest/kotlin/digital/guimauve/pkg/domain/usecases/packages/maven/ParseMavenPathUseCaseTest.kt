package digital.guimauve.pkg.domain.usecases.packages.maven

import digital.guimauve.pkg.domain.exceptions.packages.maven.InvalidMavenPathException
import digital.guimauve.pkg.domain.models.packages.maven.MavenPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParseMavenPathUseCaseTest {

    @Test
    fun testSmallWithoutVersion() {
        val usecase = ParseMavenPathUseCaseImpl()
        assertEquals(
            MavenPath(
                groupId = "digital.guimauve",
                artifactId = "pkg",
                version = null,
                filename = "maven-metadata.xml",
            ),
            usecase.invoke(listOf("digital", "guimauve", "pkg", "maven-metadata.xml")),
        )
    }

    @Test
    fun testLargeWithoutVersion() {
        val usecase = ParseMavenPathUseCaseImpl()
        assertEquals(
            MavenPath(
                groupId = "digital.guimauve.very.long.group",
                artifactId = "pkg",
                version = null,
                filename = "maven-metadata.xml",
            ),
            usecase.invoke(listOf("digital", "guimauve", "very", "long", "group", "pkg", "maven-metadata.xml")),
        )
    }

    @Test
    fun testSmallWithVersion() {
        val usecase = ParseMavenPathUseCaseImpl()
        assertEquals(
            MavenPath(
                groupId = "digital.guimauve",
                artifactId = "pkg",
                version = "1.0.0",
                filename = "pkg-1.0.0.jar",
            ),
            usecase.invoke(listOf("digital", "guimauve", "pkg", "1.0.0", "pkg-1.0.0.jar")),
        )
    }

    @Test
    fun testLargeWithVersion() {
        val usecase = ParseMavenPathUseCaseImpl()
        assertEquals(
            MavenPath(
                groupId = "digital.guimauve.very.long.group",
                artifactId = "pkg",
                version = "1.0.0",
                filename = "pkg-1.0.0.jar",
            ),
            usecase.invoke(listOf("digital", "guimauve", "very", "long", "group", "pkg", "1.0.0", "pkg-1.0.0.jar")),
        )
    }

    @Test
    fun testInvalid() {
        val usecase = ParseMavenPathUseCaseImpl()
        assertFailsWith(InvalidMavenPathException::class) {
            usecase.invoke(listOf("digital"))
        }
    }

    /**
     * A segment is decoded only after the path has been split, so `%2e%2e%2f` arrives as `../`
     * inside one segment. Any of these reaching the storage key would escape its root.
     */
    @Test
    fun testTraversingSegmentsAreRefused() {
        val usecase = ParseMavenPathUseCaseImpl()
        val traversing = listOf(
            listOf("digital", "guimauve", "pkg", "1.0.0", "../../pkg.jar"),
            listOf("digital", "guimauve", "pkg", "1.0.0", ".."),
            listOf("digital", "guimauve", "pkg", "1.0.0", "."),
            listOf("digital", "guimauve", "pkg", "1.0.0", ""),
            listOf("digital", "guimauve", "pkg", "1.0.0", "..\\..\\pkg.jar"),
            listOf("digital", "guimauve", "pkg", "1.0.0", "/etc/passwd"),
            listOf("digital", "..", "pkg", "1.0.0", "pkg-1.0.0.jar"),
        )
        traversing.forEach { segments ->
            assertFailsWith(InvalidMavenPathException::class, "accepted $segments") {
                usecase.invoke(segments)
            }
        }
    }

}
