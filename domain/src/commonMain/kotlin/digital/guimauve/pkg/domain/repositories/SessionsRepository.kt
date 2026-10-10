package digital.guimauve.pkg.domain.repositories

/** Reads and writes the serialized web sessions, so they survive a restart and are shared between instances. */
interface SessionsRepository {

    /** Reads a session. @return Its serialized value, or null if there is no such session. */
    suspend fun get(id: String): String?

    /** Writes a session, replacing its value if it already exists. */
    suspend fun set(id: String, value: String)

    /** Removes a session. Removing one that is already gone is not an error. */
    suspend fun delete(id: String)

}
