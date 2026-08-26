package intelbras.mobi.smart.persistence

import app.cash.sqldelight.db.SqlDriver
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase

internal expect fun inMemoryDriver(): SqlDriver

/** Driver sem esquema nenhum, para os testes que criam o banco de uma versão antiga na mão. */
internal expect fun emptyDriver(): SqlDriver

internal fun testDatabase(): SmartHomeDatabase = SmartHomeDatabase(inMemoryDriver())
