package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.models.organizations.Organization
import kotlin.uuid.Uuid

interface OrganizationsRepository {

    suspend fun list(): List<Organization>
    suspend fun get(id: Uuid): Organization?
    suspend fun create(payload: CreateOrganizationPayload): Organization?

}
