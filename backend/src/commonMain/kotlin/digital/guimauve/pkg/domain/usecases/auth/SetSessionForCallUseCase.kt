package digital.guimauve.pkg.domain.usecases.auth

import digital.guimauve.pkg.models.auth.SessionPayload
import io.ktor.server.application.*
import io.ktor.server.sessions.*

class SetSessionForCallUseCase : ISetSessionForCallUseCase {

    override fun invoke(input1: ApplicationCall, input2: SessionPayload) = input1.sessions.set(input2)

}
