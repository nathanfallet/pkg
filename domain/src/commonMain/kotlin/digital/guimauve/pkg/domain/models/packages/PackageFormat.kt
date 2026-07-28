package digital.guimauve.pkg.domain.models.packages

import kotlinx.serialization.Serializable

@Serializable
enum class PackageFormat {

    MAVEN, NPM, PYPI

}
