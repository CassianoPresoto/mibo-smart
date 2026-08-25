package intelbras.mobi.smart.persistence

import app.cash.sqldelight.db.SqlDriver
import intelbras.mobi.smart.persistence.auth.AccessTokenSecretStore

interface SmartHomePersistenceFactory {
    fun createDriver(): SqlDriver

    fun createSecretStore(): AccessTokenSecretStore
}

internal const val SMART_HOME_DATABASE_NAME = "smart_home.db"

internal const val ACCESS_TOKEN_SECRET_KEY = "access_token"
