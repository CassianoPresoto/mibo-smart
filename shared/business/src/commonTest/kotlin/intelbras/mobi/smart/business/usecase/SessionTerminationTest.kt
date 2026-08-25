package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.InMemoryAccessTokenStore
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class SessionTerminationTest {

    @Test
    fun `signing out discards the stored token`() = runTest {
        val accessTokenStore = InMemoryAccessTokenStore()
        accessTokenStore.save("Ot_token")

        SessionTermination(accessTokenStore)()

        assertNull(accessTokenStore.currentAccessToken())
    }
}
