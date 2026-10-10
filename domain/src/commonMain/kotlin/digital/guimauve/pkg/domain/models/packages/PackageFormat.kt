package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable

/** The ecosystem a package belongs to. Its name is what gets stored in the database. */
@Serializable
enum class PackageFormat {

    MAVEN, NPM, PYPI

}
