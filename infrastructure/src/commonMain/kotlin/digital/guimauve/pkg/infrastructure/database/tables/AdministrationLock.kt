package digital.guimauve.pkg.infrastructure.database.tables

import org.jetbrains.exposed.v1.core.Table

/** A persistent singleton: serializes bootstrap and administrator mutations across replicas. */
object AdministrationLock : Table() {
    val id = integer("id")
    override val primaryKey = PrimaryKey(id)
}
