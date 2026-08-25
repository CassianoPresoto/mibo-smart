package intelbras.mobi.smart.persistence

import app.cash.sqldelight.db.SqlDriver
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase

internal expect fun inMemoryDriver(): SqlDriver

internal fun testDatabase(): SmartHomeDatabase = SmartHomeDatabase(inMemoryDriver())
