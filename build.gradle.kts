plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.maven) apply false
    alias(libs.plugins.kover)
}

allprojects {
    group = "digital.guimauve.pkg"
    version = "0.1.3"
    project.ext.set("url", "https://github.com/guimauvedigital/pkg")
    project.ext.set("license.name", "Apache 2.0")
    project.ext.set("license.url", "https://www.apache.org/licenses/LICENSE-2.0.txt")
    project.ext.set("developer.id", "nathanfallet")
    project.ext.set("developer.name", "Nathan Fallet")
    project.ext.set("developer.email", "contact@nathanfallet.me")
    project.ext.set("developer.url", "https://www.nathanfallet.me")
    project.ext.set("scm.url", "https://github.com/guimauvedigital/pkg.git")

    repositories {
        mavenCentral()
    }
}

dependencies {
    kover(projects.app)
    kover(projects.backend)
    kover(projects.commons)
}
