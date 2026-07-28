package digital.guimauve.pkg.api.models.packages.npm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The body `npm publish` sends: the packument of what is being published, plus the tarballs
 * themselves, base64 encoded, keyed by file name.
 */
@Serializable
data class NpmPublishRequest(
    val name: String,
    @SerialName("dist-tags") val distTags: Map<String, String> = emptyMap(),
    val versions: Map<String, JsonObject> = emptyMap(),
    @SerialName("_attachments") val attachments: Map<String, NpmAttachment> = emptyMap(),
)

@Serializable
data class NpmAttachment(
    @SerialName("content_type") val contentType: String = "application/octet-stream",
    val data: String,
    val length: Long = 0,
)
