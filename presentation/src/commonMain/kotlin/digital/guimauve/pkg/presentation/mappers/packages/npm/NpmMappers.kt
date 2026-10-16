package digital.guimauve.pkg.presentation.mappers.packages.npm

import digital.guimauve.pkg.api.models.packages.npm.NpmPackument
import digital.guimauve.pkg.domain.models.packages.Package
import digital.guimauve.pkg.domain.models.packages.versions.PackageVersion
import kotlinx.serialization.json.*
import kotlin.uuid.Uuid

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
 * @param distTags The tags of the package, each with the identifier of the version it points to. A
 * tag pointing at a version not in [versions] is left out.
 * @param baseUrl Where this instance serves the npm registry from, without a trailing slash.
 */
fun Package.toNpmPackument(
    versions: List<PackageVersion>,
    distTags: Map<String, Uuid>,
    baseUrl: String,
): NpmPackument {
    val manifests = versions.mapNotNull { version ->
        val manifest = version.manifest() ?: return@mapNotNull null
        val url = "$baseUrl/${name}/-/${tarballName(name, version.version)}"
        version.version to manifest.withTarball(url)
    }.toMap()

    val names = versions.associate { it.id to it.version }
    val tags = distTags.mapNotNull { (tag, versionId) -> names[versionId]?.let { tag to it } }.toMap()

    // npm cannot resolve a package without `latest`. A package published before tags were recorded,
    // or only ever under another tag, gets its most recently published version, as npm's own
    // registry does.
    val latest = tags["latest"] ?: versions.maxByOrNull { it.publishedAt }?.version

    return NpmPackument(
        id = name,
        name = name,
        distTags = tags + (latest?.let { mapOf("latest" to it) } ?: emptyMap()),
        versions = manifests,
    )
}
