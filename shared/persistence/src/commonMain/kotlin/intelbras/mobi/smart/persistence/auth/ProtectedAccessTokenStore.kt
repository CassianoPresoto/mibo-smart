package intelbras.mobi.smart.persistence.auth

import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.domain.auth.model.AccessToken
import intelbras.mobi.smart.persistence.db.SmartHomeDatabase
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal class ProtectedAccessTokenStore(
    database: SmartHomeDatabase,
    private val secretStore: AccessTokenSecretStore,
    private val ioDispatcher: CoroutineDispatcher,
) : AccessTokenStore {

    private val queries = database.accessTokenQueries

    override suspend fun read(): AccessToken? = withContext(ioDispatcher) {
        val expiration = queries.selectExpiration(SINGLE_SESSION_ID).executeAsOneOrNull()
        val token = secretStore.read()

        if (expiration == null || token == null) {
            discard()
            return@withContext null
        }
        AccessToken(value = token, expiresAt = Instant.fromEpochMilliseconds(expiration))
    }

    override suspend fun save(accessToken: AccessToken): Unit = withContext(ioDispatcher) {
        secretStore.write(accessToken.value)
        queries.replaceExpiration(
            id = SINGLE_SESSION_ID,
            expiresAtEpochMilliseconds = accessToken.expiresAt.toEpochMilliseconds(),
        )
    }

    override suspend fun clear(): Unit = withContext(ioDispatcher) {
        discard()
    }

    private fun discard() {
        secretStore.clear()
        queries.deleteExpiration(SINGLE_SESSION_ID)
    }

    private companion object {
        const val SINGLE_SESSION_ID = 1L
    }
}
