package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.domain.auth.model.AccessToken
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class SessionTerminationTest {

    @Test
    fun `signing out discards the stored token`() = runTest {
        val accessTokenStore = InMemoryAccessTokenStore(
            AccessToken("Ot_token", NOW + AccessToken.LIFETIME)
        )

        SessionTermination(accessTokenStore)()

        assertNull(accessTokenStore.read())
    }
}
