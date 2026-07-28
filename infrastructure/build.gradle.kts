plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("pkg-infrastructure")
        description.set("Implementations of the PKG domain contracts: database, storage, tokens.")
        url.set(project.ext.get("url")?.toString())
        licenses {
            license {
                name.set(project.ext.get("license.name")?.toString())
                url.set(project.ext.get("license.url")?.toString())
            }
        }
        developers {
            developer {
                id.set(project.ext.get("developer.id")?.toString())
                name.set(project.ext.get("developer.name")?.toString())
                email.set(project.ext.get("developer.email")?.toString())
                url.set(project.ext.get("developer.url")?.toString())
            }
        }
        scm {
            url.set(project.ext.get("scm.url")?.toString())
        }
    }
}

kotlin {
    jvmToolchain(21)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        all {
            languageSettings.apply {
                optIn("kotlin.uuid.ExperimentalUuidApi")
            }
        }
        val commonMain by getting {
            dependencies {
                api(projects.domain)

                api(libs.bundles.exposed)
                api(libs.hikari)
                api(libs.mysql)
                api(libs.s3)
                api(libs.bcrypt)

                api(libs.koin.ktor)
                api(libs.ktor.server.core)
                api(libs.ktor.server.auth.jwt)
                api(libs.ktor.server.sessions)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.tests.mockk)
            }
        }
    }
}
