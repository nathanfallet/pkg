plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.2.1"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

group = "digital.guimauve.pkg"
version = "0.1.3"

gradlePlugin {
    website = "https://github.com/nathanfallet/pkg"
    vcsUrl = "https://github.com/nathanfallet/pkg.git"

    plugins {
        create("pkg-gradle-plugin") {
            id = "digital.guimauve.pkg"
            implementationClass = "digital.guimauve.pkg.PKGPlugin"
            displayName = "PKG Gradle Plugin"
            description = "Simply configure your custom repository on every project."
            tags = listOf("maven", "repository", "guimauve")
        }
    }
}
