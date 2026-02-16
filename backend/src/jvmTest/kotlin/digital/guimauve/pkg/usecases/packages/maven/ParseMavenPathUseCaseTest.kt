package digital.guimauve.pkg.usecases.packages.maven

import dev.kaccelero.commons.exceptions.ControllerException
import digital.guimauve.pkg.domain.usecases.packages.maven.ParseMavenPathUseCase
import digital.guimauve.pkg.models.packages.maven.MavenPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParseMavenPathUseCaseTest {

    @Test
    fun testSmallWithoutVersion() {
        val usecase = ParseMavenPathUseCase()
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
        val usecase = ParseMavenPathUseCase()
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
        val usecase = ParseMavenPathUseCase()
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
        val usecase = ParseMavenPathUseCase()
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
        val usecase = ParseMavenPathUseCase()
        assertFailsWith(ControllerException::class) {
            usecase.invoke(listOf("digital"))
        }
    }

}
