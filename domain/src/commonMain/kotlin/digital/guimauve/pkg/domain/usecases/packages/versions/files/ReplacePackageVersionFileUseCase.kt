package digital.guimauve.pkg.domain.usecases.packages.versions.files

import digital.guimauve.pkg.domain.models.packages.versions.files.PackageVersionFile
import digital.guimauve.pkg.domain.models.storage.FileFromStream

/**
 * Overwrites the content of a file that is rewritten on every publication, such as the
 * `maven-metadata.xml` of a maven module and its checksums: the client reads it, adds the version it
 * publishes, and writes it back. The files of a version stay immutable and never go through here.
 */
interface ReplacePackageVersionFileUseCase {

    /**
     * @return The file with its new size and type, or null if the storage did not keep the content.
     */
    suspend operator fun invoke(file: PackageVersionFile, source: FileFromStream): PackageVersionFile?

}
