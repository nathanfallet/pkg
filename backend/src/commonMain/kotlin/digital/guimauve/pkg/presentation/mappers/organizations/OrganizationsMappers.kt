package digital.guimauve.pkg.presentation.mappers.organizations

import digital.guimauve.pkg.api.responses.organizations.OrganizationResponse
import digital.guimauve.pkg.models.organizations.Organization

/**
 * Maps a [Organization] to a [OrganizationResponse].
 *
 * @return The mapped [OrganizationResponse].
 */
fun Organization.toOrganizationResponse() = OrganizationResponse(
    id = id,
    name = name,
)
