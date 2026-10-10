plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kover)
    application
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

application {
    mainClass.set("digital.guimauve.pkg.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_21)
        localImageName.set("pkg")
        findProperty("imageTag")?.let { imageTag.set(it.toString()) }

        // Published by CI: on every push to main (latest) and on each tag (see .github/workflows).
        // The image lives in the `guimauvedigital` namespace, but Docker Hub logins go through the
        // `guimauvedigitalholder` account (the one DOCKER_HUB_PASSWORD belongs to). `dockerHub()` would
        // derive the image name from the username, so the namespace is set separately here.
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.externalRegistry(
                username = provider { "guimauvedigitalholder" },
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD"),
                project = provider { "pkg" },
                namespace = provider { "guimauvedigital" },
            )
        )
    }
}

dependencies {
    implementation(projects.api)
    implementation(projects.domain)
    implementation(projects.infrastructure)
    implementation(projects.presentation)

    implementation(libs.logback.core)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.netty)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.h2)
}

// flyway-core and flyway-mysql each ship META-INF/services/org.flywaydb.core.extensibility.Plugin. Shadow
// keeps only one by default; Flyway then loses its core extensions and the migration crashes at boot
// (an NPE in PluginRegister), which no test catches since tests do not run on the fat jar. Merge the
// service files, and keep the default "first wins" for everything else.
tasks.shadowJar {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    mergeServiceFiles()
    filesNotMatching("META-INF/services/**") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("jvmTest") {
    dependsOn("test") // To run normal `jvm` module `test` task when running all `jvmTest` tasks.
}
