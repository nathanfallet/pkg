package digital.guimauve.pkg.presentation.mappers.organizations

import digital.guimauve.pkg.api.responses.organizations.OrganizationResponse
import digital.guimauve.pkg.domain.models.organizations.Organization
import digital.guimauve.pkg.presentation.views.OrganizationView

/**
 * Maps an [Organization] to an [OrganizationResponse].
 *
 * @return The mapped [OrganizationResponse].
 */
fun Organization.toOrganizationResponse() = OrganizationResponse(
    id = id,
    name = name,
)

/**
 * Maps an [Organization] to an [OrganizationView].
 *
 * @return The mapped [OrganizationView].
 */
fun Organization.toOrganizationView() = OrganizationView(
    id = id.toString(),
    name = name,
)
