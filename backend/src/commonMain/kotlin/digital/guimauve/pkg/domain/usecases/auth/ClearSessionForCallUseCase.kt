package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.models.auth.SessionPayload
import io.ktor.server.application.*
import io.ktor.server.sessions.*

class ClearSessionForCallUseCase : IClearSessionForCallUseCase {

    override fun invoke(input: ApplicationCall) = input.sessions.clear<SessionPayload>()

}
