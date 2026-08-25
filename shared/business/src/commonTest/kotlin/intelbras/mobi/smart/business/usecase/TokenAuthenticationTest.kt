package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.emptyPage
import intelbras.mobi.smart.business.session.InMemoryAccessTokenStore
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class TokenAuthenticationTest {

    private val accessTokenStore = InMemoryAccessTokenStore()

    @Test
    fun `a blank token never reaches the platform`() = runTest {
        val deviceRepository = mock<DeviceRepository>()

        val result = TokenAuthentication(accessTokenStore, deviceRepository)("   ")

        assertEquals(AuthenticationResult.MissingToken, result)
        assertNull(accessTokenStore.currentAccessToken())
        verifySuspend(not) { deviceRepository.listDevices(any()) }
    }

    @Test
    fun `a valid token is kept after the verification call`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } returns emptyPage()
        }

        val result = TokenAuthentication(accessTokenStore, deviceRepository)("  Ot_token  ")

        assertEquals(AuthenticationResult.Success, result)
        assertEquals("Ot_token", accessTokenStore.currentAccessToken())
        verifySuspend {
            deviceRepository.listDevices(DeviceListQuery(page = DeviceListQuery.FIRST_PAGE, pageSize = 1))
        }
    }

    @Test
    fun `a token refused by the platform is not kept`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws SmartHomeUnauthorizedException("HTTP 401")
        }

        val result = TokenAuthentication(accessTokenStore, deviceRepository)("Ot_expired")

        assertEquals(AuthenticationResult.InvalidToken, result)
        assertNull(accessTokenStore.currentAccessToken())
    }

    @Test
    fun `the platform unknown error is treated as an expired token`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws
                SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
        }

        val result = TokenAuthentication(accessTokenStore, deviceRepository)("Ot_expired")

        assertEquals(AuthenticationResult.InvalidToken, result)
        assertNull(accessTokenStore.currentAccessToken())
    }

    @Test
    fun `a network failure does not invalidate the typed token`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws SmartHomeNetworkException()
        }

        val result = TokenAuthentication(accessTokenStore, deviceRepository)("Ot_token")

        assertEquals(AuthenticationResult.NetworkUnavailable, result)
    }
}
