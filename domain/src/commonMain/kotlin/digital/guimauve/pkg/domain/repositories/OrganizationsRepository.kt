package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.domain.models.organizations.Organization
import kotlin.uuid.Uuid

interface OrganizationsRepository {

    suspend fun get(id: Uuid): Organization?
    suspend fun create(payload: CreateOrganizationPayload): Organization?

}
