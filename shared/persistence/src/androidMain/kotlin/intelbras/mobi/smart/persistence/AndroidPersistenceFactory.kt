package intelbras.mobi.smart.persistence

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import intelbras.mobi.smart.domain.capture.MediaFileStore
import intelbras.mobi.smart.persistence.auth.AccessTokenSecretStore
import intelbras.mobi.smart.persistence.auth.KeystoreAccessTokenSecretStore
import intelbras.mobi.smart.persistence.capture.FileMediaFileStore
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import kotlinx.coroutines.Dispatchers

class AndroidPersistenceFactory(private val context: Context) : SmartHomePersistenceFactory {

    override fun createDriver(): SqlDriver = AndroidSqliteDriver(
        schema = SmartHomeDatabase.Schema,
        context = context,
        name = SMART_HOME_DATABASE_NAME,
    )

    override fun createSecretStore(): AccessTokenSecretStore =
        KeystoreAccessTokenSecretStore(context)

    override fun createMediaFileStore(): MediaFileStore =
        FileMediaFileStore(context.filesDir, Dispatchers.IO)
}
