package intelbras.mobi.smart.business

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import intelbras.mobi.smart.business.session.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionTermination
import intelbras.mobi.smart.business.usecase.TokenAuthentication
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class SmartHomeSessionTest {

    private val accessTokenStore = InMemoryAccessTokenStore()

    @Test
    fun `authenticating keeps the token that the platform accepted`() = runTest {
        val session = sessionWith(
            mock<DeviceRepository> { everySuspend { listDevices(any()) } returns emptyPage() }
        )

        assertEquals(AuthenticationResult.Success, session.authenticate("Ot_token"))
        assertEquals("Ot_token", accessTokenStore.currentAccessToken())
    }

    @Test
    fun `authenticating reports a token the platform refused`() = runTest {
        val session = sessionWith(
            mock<DeviceRepository> {
                everySuspend { listDevices(any()) } throws SmartHomeUnauthorizedException("HTTP 401")
            }
        )

        assertEquals(AuthenticationResult.InvalidToken, session.authenticate("Ot_expired"))
        assertNull(accessTokenStore.currentAccessToken())
    }

    @Test
    fun `signing out discards the stored token`() = runTest {
        val session = sessionWith(
            mock<DeviceRepository> { everySuspend { listDevices(any()) } returns emptyPage() }
        )
        session.authenticate("Ot_token")

        session.signOut()

        assertNull(accessTokenStore.currentAccessToken())
    }

    private fun sessionWith(deviceRepository: DeviceRepository): SmartHomeSession =
        SmartHomeSessionImpl(
            tokenAuthentication = TokenAuthentication(accessTokenStore, deviceRepository),
            sessionTermination = SessionTermination(accessTokenStore),
        )
}
