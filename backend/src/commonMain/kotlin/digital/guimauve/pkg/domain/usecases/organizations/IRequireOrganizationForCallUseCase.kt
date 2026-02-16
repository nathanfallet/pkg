package digital.guimauve.pkg.domain.usecases.organizations

import dev.kaccelero.usecases.ISuspendUseCase
import digital.guimauve.pkg.models.organizations.Organization
import io.ktor.server.application.*

interface IRequireOrganizationForCallUseCase : ISuspendUseCase<ApplicationCall, Organization>
