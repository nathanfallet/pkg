package digital.guimauve.pkg.infrastructure.database

/**
 * Connection settings for the database.
 *
 * [directory] is where the local H2 file database lives (blank = in-memory, used by tests); [host]
 * is the MySQL server host. Only one applies, depending on [protocol].
 */
data class DatabaseConfig(
    val protocol: String,
    val name: String,
    val directory: String = "",
    val host: String = "localhost",
    val port: Int = 3306,
    val user: String = "root",
    val password: String = "",
    val maximumPoolSize: Int = 10,
)
