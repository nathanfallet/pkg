package digital.guimauve.pkg.presentation.mappers.packages.npm

import digital.guimauve.pkg.api.models.packages.npm.NpmPackument
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlinx.serialization.json.*

/**
 * The name of the archive published for a version, as npm builds it: the scope becomes a prefix.
 */
fun tarballName(packageName: String, version: String): String =
    packageName.removePrefix("@").replace('/', '-') + "-" + version + ".tgz"

/**
 * Reads back the manifest stored when the version was published.
 *
 * @return The manifest, or null if the version carries none — a version published through another
 * registry format, or before this one understood npm.
 */
private fun PackageVersion.manifest(): JsonObject? = metadata
    ?.let { runCatching { Json.parseToJsonElement(it).jsonObject }.getOrNull() }

/**
 * Rewrites the `dist.tarball` of a manifest to point at this instance.
 *
 * The url the publisher sent names whatever registry it was configured with, so it is never the
 * one to serve back.
 */
private fun JsonObject.withTarball(url: String): JsonObject = buildJsonObject {
    this@withTarball.forEach { (key, value) ->
        if (key != "dist") put(key, value)
    }
    put("dist", buildJsonObject {
        this@withTarball["dist"]?.jsonObject?.forEach { (key, value) ->
            if (key != "tarball") put(key, value)
        }
        put("tarball", url)
    })
}

/**
 * Assembles the document npm reads to resolve a package.
 *
 * @param baseUrl Where this instance serves the npm registry from, without a trailing slash.
 */
fun Package.toNpmPackument(versions: List<PackageVersion>, baseUrl: String): NpmPackument {
    val manifests = versions.mapNotNull { version ->
        val manifest = version.manifest() ?: return@mapNotNull null
        val url = "$baseUrl/${name}/-/${tarballName(name, version.version)}"
        version.version to manifest.withTarball(url)
    }.toMap()

    // Nothing records the tags a publisher asked for, so the newest version is the latest one.
    val latest = versions.maxByOrNull { it.publishedAt }?.version

    return NpmPackument(
        id = name,
        name = name,
        distTags = latest?.let { mapOf("latest" to it) } ?: emptyMap(),
        versions = manifests,
    )
}
