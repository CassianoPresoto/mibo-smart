package intelbras.mobi.smart.persistence

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase

internal actual fun inMemoryDriver(): SqlDriver {
    val schema = SmartHomeDatabase.Schema
    return NativeSqliteDriver(
        DatabaseConfiguration(
            name = "smart_home_test.db",
            version = schema.version.toInt(),
            inMemory = true,
            create = { connection -> wrapConnection(connection) { schema.create(it) } },
        )
    )
}

private var emptyDatabaseCount = 0

internal actual fun emptyDriver(): SqlDriver = NativeSqliteDriver(
    DatabaseConfiguration(
        name = "smart_home_empty_${emptyDatabaseCount++}.db",
        version = 1,
        inMemory = true,
        create = { },
    )
)
