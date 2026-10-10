package digital.guimauve.pkg.domain.models.organizations

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/** A group of users that owns packages. Every user and every package belongs to exactly one. */
@Serializable
data class Organization(
    val id: Uuid,
    val name: String,
)
