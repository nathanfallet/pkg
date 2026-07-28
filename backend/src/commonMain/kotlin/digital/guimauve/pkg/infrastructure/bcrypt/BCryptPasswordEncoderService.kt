package digital.guimauve.pkg.infrastructure.bcrypt

import at.favre.lib.crypto.bcrypt.BCrypt
import digital.guimauve.pkg.domain.services.PasswordEncoderService

/**
 * Implementation of [PasswordEncoderService] using BCrypt hashing algorithm.
 */
class BCryptPasswordEncoderService : PasswordEncoderService {

    override fun encode(rawPassword: String): String =
        BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray())

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        BCrypt.verifyer().verify(rawPassword.toCharArray(), encodedPassword).verified

}
