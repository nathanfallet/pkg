package digital.guimauve.pkg.infrastructure.database

import dev.kaccelero.database.IDatabase
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class Database(
    protocol: String,
    host: String = "",
    name: String = "",
    user: String = "",
    password: String = "",
) : IDatabase {

    // Connect to database
    private val database: Database = when (protocol) {
        "mysql" -> Database.Companion.connect(
            "jdbc:mysql://$host:3306/$name", "com.mysql.cj.jdbc.Driver",
            user, password
        )

        "h2" -> Database.Companion.connect(
            "jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1;", "org.h2.Driver"
        )

        else -> throw Exception("Unsupported database protocol: $protocol")
    }

    override fun <T> transaction(statement: Transaction.() -> T): T =
        org.jetbrains.exposed.sql.transactions.transaction(database, statement)

    override suspend fun <T> suspendedTransaction(statement: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { statement() }

}
