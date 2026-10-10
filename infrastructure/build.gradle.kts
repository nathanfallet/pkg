plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.maven)
    alias(libs.plugins.exposed)
}

mavenPublishing {
    publishToMavenCentral()
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
                api(libs.bundles.flyway)
                api(libs.hikari)
                api(libs.mysql)
                api(libs.s3)
                api(libs.bcrypt)
                api(libs.bundles.opentelemetry)

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
                implementation(libs.opentelemetry.sdk.testing)
                implementation(libs.h2)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.exposed.migration.jdbc)
                implementation(libs.tests.testcontainers.mysql)
            }
        }
    }
}

/*
 * `./gradlew :infrastructure:generateMigrations` — starts a throwaway MySQL (Testcontainers, so Docker
 * must be running), replays every migration already in `db/migration` with Flyway, diffs the result
 * against the Exposed tables and writes the missing DDL as a new `V<timestamp>__….sql`. It never
 * applies anything: the app does, through Flyway, at boot.
 *
 * ⚠️ Always read the generated file before keeping it. The diff turns a rename into ADD + DROP (data
 * lost), misses most type changes on MySQL, and only sees an index's columns and uniqueness.
 */
exposed {
    migrations {
        tablesPackage = "digital.guimauve.pkg.infrastructure.database.tables"
        // The same major as the Bitnami MySQL chart in helm/, so the diff is computed against the server we run.
        testContainersImageName = "mysql:8.0"
        fileDirectory = layout.projectDirectory.dir("src/commonMain/resources/db/migration")
        // Multiplatform: there is no `main` source set for the plugin to default to.
        classpath.from(kotlin.jvm().compilations.named("main").map { it.output.allOutputs + it.runtimeDependencyFiles })
    }
}
