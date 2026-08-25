package intelbras.mobi.smart.persistence

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase

internal actual fun inMemoryDriver(): SqlDriver =
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also(SmartHomeDatabase.Schema::create)
