package digital.guimauve.pkg.domain.repositories

import digital.guimauve.pkg.domain.models.organizations.CreateOrganizationPayload
import digital.guimauve.pkg.domain.models.organizations.Organization
import kotlin.uuid.Uuid

/** Reads and writes the organizations, which own the users and the packages. */
interface OrganizationsRepository {

    /** Reads an organization by its identifier. */
    suspend fun get(id: Uuid): Organization?

    /** Registers an organization. @return The created organization, or null if the insert returned no row. */
    suspend fun create(payload: CreateOrganizationPayload): Organization?

}
