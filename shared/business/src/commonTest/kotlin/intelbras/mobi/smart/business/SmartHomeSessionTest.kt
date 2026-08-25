package intelbras.mobi.smart.business

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionInspection
import intelbras.mobi.smart.business.usecase.SessionStatus
import intelbras.mobi.smart.business.usecase.SessionTermination
import intelbras.mobi.smart.business.usecase.TokenAuthentication
import intelbras.mobi.smart.domain.auth.model.AccessToken
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SmartHomeSessionTest {

    private val accessTokenStore = InMemoryAccessTokenStore()
    private val clock = FixedClock()

    @Test
    fun `authenticating keeps the session the platform accepted`() = runTest {
        val session = sessionWith(
            mock<DeviceRepository> { everySuspend { listDevices(any()) } returns emptyPage() }
        )

        val expiresAt = NOW + AccessToken.LIFETIME
        assertEquals(AuthenticationResult.Success(expiresAt), session.authenticate("Ot_token"))
        assertEquals(SessionStatus.Active(expiresAt), session.currentStatus())
    }

    @Test
    fun `authenticating reports a token the platform refused`() = runTest {
        val session = sessionWith(
            mock<DeviceRepository> {
                everySuspend { listDevices(any()) } throws SmartHomeUnauthorizedException("HTTP 401")
            }
        )

        assertEquals(AuthenticationResult.InvalidToken, session.authenticate("Ot_expired"))
        assertEquals(SessionStatus.None, session.currentStatus())
    }

    @Test
    fun `signing out ends the active session`() = runTest {
        val session = sessionWith(
            mock<DeviceRepository> { everySuspend { listDevices(any()) } returns emptyPage() }
        )
        session.authenticate("Ot_token")

        session.signOut()

        assertEquals(SessionStatus.None, session.currentStatus())
    }

    private fun sessionWith(deviceRepository: DeviceRepository): SmartHomeSession =
        SmartHomeSessionImpl(
            tokenAuthentication = TokenAuthentication(accessTokenStore, deviceRepository, clock),
            sessionInspection = SessionInspection(accessTokenStore, clock),
            sessionTermination = SessionTermination(accessTokenStore),
        )
}
