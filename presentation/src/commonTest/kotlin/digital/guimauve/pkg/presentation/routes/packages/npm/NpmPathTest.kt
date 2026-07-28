package digital.guimauve.pkg.presentation.routes.packages.npm

import digital.guimauve.pkg.domain.exceptions.packages.PackageNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The shapes below were captured from a real `npm publish` and `npm install`, which do not spell a
 * package name the same way: the scope separator is encoded for a document and not for a tarball.
 */
class NpmPathTest {

    @Test
    fun testScopedPackument() {
        // `GET /npm/@guimauve%2ffake-pkg` — one segment once decoded by the router.
        assertEquals(
            NpmPath.Packument("@guimauve/fake-pkg"),
            parseNpmPath(listOf("@guimauve/fake-pkg")),
        )
        // The same name spelled out, which a human or curl would send.
        assertEquals(
            NpmPath.Packument("@guimauve/fake-pkg"),
            parseNpmPath(listOf("@guimauve", "fake-pkg")),
        )
    }

    @Test
    fun testUnscopedPackument() {
        assertEquals(NpmPath.Packument("fake-pkg"), parseNpmPath(listOf("fake-pkg")))
    }

    @Test
    fun testVersion() {
        assertEquals(NpmPath.Version("fake-pkg", "1.0.0"), parseNpmPath(listOf("fake-pkg", "1.0.0")))
        assertEquals(
            NpmPath.Version("@guimauve/fake-pkg", "1.0.0"),
            parseNpmPath(listOf("@guimauve", "fake-pkg", "1.0.0")),
        )
    }

    @Test
    fun testTarball() {
        // `GET /npm/@guimauve/fake-pkg/-/guimauve-fake-pkg-1.0.0.tgz`, scope not encoded.
        assertEquals(
            NpmPath.Tarball("@guimauve/fake-pkg", "guimauve-fake-pkg-1.0.0.tgz"),
            parseNpmPath(listOf("@guimauve", "fake-pkg", "-", "guimauve-fake-pkg-1.0.0.tgz")),
        )
        assertEquals(
            NpmPath.Tarball("fake-pkg", "fake-pkg-1.0.0.tgz"),
            parseNpmPath(listOf("fake-pkg", "-", "fake-pkg-1.0.0.tgz")),
        )
    }

    @Test
    fun testNothing() {
        assertFailsWith(PackageNotFoundException::class) { parseNpmPath(emptyList()) }
        assertFailsWith(PackageNotFoundException::class) {
            parseNpmPath(listOf("fake-pkg", "1.0.0", "extra"))
        }
    }

}
