package digital.guimauve.pkg.domain.models.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.PackageFormat
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * The storage key of a file. It is built from ids only, never from the package name, so a name an
 * uploader chooses cannot place a file anywhere but under its own organization, package and version.
 */
class CreatePackageVersionFilePayloadTest {

    @Test
    fun `a file is stored under its organization, package and version`() {
        val now = Clock.System.now()
        val pkg = Package(Uuid.random(), "../../evil", PackageFormat.MAVEN, Uuid.random(), false, now)
        val version = PackageVersion(Uuid.random(), pkg.id, "1.0.0", Uuid.random(), now, null, false)

        val payload = CreatePackageVersionFilePayload("pkg-1.0.0.jar", pkg, version)

        assertEquals("pkg-1.0.0.jar", payload.name)
        assertEquals("${pkg.organizationId}/${pkg.id}/${version.id}/pkg-1.0.0.jar", payload.path)
    }

}
