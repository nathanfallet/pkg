package digital.guimauve.pkg.domain.repositories

interface SessionsRepository {

    suspend fun get(id: String): String?
    suspend fun set(id: String, value: String)
    suspend fun delete(id: String)

}
