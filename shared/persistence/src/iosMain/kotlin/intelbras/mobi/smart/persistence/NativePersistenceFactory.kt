package intelbras.mobi.smart.persistence

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.persistence.auth.AccessTokenSecretStore
import intelbras.mobi.smart.persistence.auth.KeychainAccessTokenSecretStore
import intelbras.mobi.smart.persistence.capture.NativeMediaFileStore
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import kotlinx.coroutines.Dispatchers

class NativePersistenceFactory : SmartHomePersistenceFactory {

    override fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = SmartHomeDatabase.Schema,
        name = SMART_HOME_DATABASE_NAME,
    )

    override fun createSecretStore(): AccessTokenSecretStore = KeychainAccessTokenSecretStore()

    override fun createMediaFileStore(): MediaFileStore = NativeMediaFileStore(Dispatchers.Default)
}
