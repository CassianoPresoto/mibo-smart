package intelbras.mobi.smart.persistence.auth

import intelbras.mobi.smart.domain.auth.model.AccessToken
import intelbras.mobi.smart.persistence.testDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest

class ProtectedAccessTokenStoreTest {

    private val database = testDatabase()
    private val secretStore = InMemorySecretStore()
    private val store = ProtectedAccessTokenStore(database, secretStore, Dispatchers.Default)
    private val expiresAt = Instant.fromEpochMilliseconds(1_800_000_000_000)

    @Test
    fun `reads nothing before any token is saved`() = runTest {
        assertNull(store.read())
    }

    @Test
    fun `reads back the token and its expiration`() = runTest {
        val token = AccessToken(value = "Ot_token", expiresAt = expiresAt)

        store.save(token)

        assertEquals(token, store.read())
    }

    @Test
    fun `keeps the token value out of the database`() = runTest {
        store.save(AccessToken(value = "Ot_token", expiresAt = expiresAt))

        val storedColumns = database.accessTokenQueries.selectExpiration(1L).executeAsOne()

        assertEquals(expiresAt.toEpochMilliseconds(), storedColumns)
        assertEquals("Ot_token", secretStore.read())
    }

    @Test
    fun `saving again replaces the previous session instead of adding a second one`() = runTest {
        store.save(AccessToken(value = "Ot_first", expiresAt = expiresAt))

        val renewed = AccessToken(
            value = "Ot_second",
            expiresAt = Instant.fromEpochMilliseconds(1_900_000_000_000),
        )
        store.save(renewed)

        assertEquals(renewed, store.read())
    }

    @Test
    fun `clearing removes both the secret and the expiration`() = runTest {
        store.save(AccessToken(value = "Ot_token", expiresAt = expiresAt))

        store.clear()

        assertNull(store.read())
        assertNull(secretStore.read())
    }

    @Test
    fun `clearing an empty store is harmless`() = runTest {
        store.clear()

        assertNull(store.read())
    }

    @Test
    fun `a secret lost by the platform discards the orphan expiration`() = runTest {
        store.save(AccessToken(value = "Ot_token", expiresAt = expiresAt))
        secretStore.clear()

        assertNull(store.read())
        assertNull(database.accessTokenQueries.selectExpiration(1L).executeAsOneOrNull())
    }

    @Test
    fun `an expiration lost by the platform discards the orphan secret`() = runTest {
        store.save(AccessToken(value = "Ot_token", expiresAt = expiresAt))
        database.accessTokenQueries.deleteExpiration(1L)

        assertNull(store.read())
        assertNull(secretStore.read())
    }
}
