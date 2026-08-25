package intelbras.mobi.smart.business.usecase

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.FixedClock
import intelbras.mobi.smart.business.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.NOW
import intelbras.mobi.smart.business.emptyPage
import intelbras.mobi.smart.domain.auth.model.AccessToken
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
    private val clock = FixedClock()

    @Test
    fun `a blank token never reaches the platform`() = runTest {
        val deviceRepository = mock<DeviceRepository>()

        val result = authentication(deviceRepository)("   ")

        assertEquals(AuthenticationResult.MissingToken, result)
        assertNull(accessTokenStore.read())
        verifySuspend(not) { deviceRepository.listDevices(any()) }
    }

    @Test
    fun `an accepted token is stored with two hours of validity`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } returns emptyPage()
        }

        val result = authentication(deviceRepository)("  Ot_token  ")

        val expiresAt = NOW + AccessToken.LIFETIME
        assertEquals(AuthenticationResult.Success(expiresAt), result)
        assertEquals(AccessToken(value = "Ot_token", expiresAt = expiresAt), accessTokenStore.read())
    }

    @Test
    fun `the token is validated against the platform with the smallest possible page`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } returns emptyPage()
        }

        authentication(deviceRepository)("Ot_token")

        verifySuspend {
            deviceRepository.listDevices(DeviceListQuery(page = DeviceListQuery.FIRST_PAGE, pageSize = 1))
        }
    }

    @Test
    fun `a token refused by the platform is not kept`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws SmartHomeUnauthorizedException("HTTP 401")
        }

        val result = authentication(deviceRepository)("Ot_expired")

        assertEquals(AuthenticationResult.InvalidToken, result)
        assertNull(accessTokenStore.read())
    }

    @Test
    fun `the platform unknown error is treated as an expired token`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws
                SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
        }

        val result = authentication(deviceRepository)("Ot_expired")

        assertEquals(AuthenticationResult.InvalidToken, result)
        assertNull(accessTokenStore.read())
    }

    @Test
    fun `a network failure is reported without keeping the token`() = runTest {
        val deviceRepository = mock<DeviceRepository> {
            everySuspend { listDevices(any()) } throws SmartHomeNetworkException()
        }

        val result = authentication(deviceRepository)("Ot_token")

        assertEquals(AuthenticationResult.NetworkUnavailable, result)
        assertNull(accessTokenStore.read())
    }

    private fun authentication(deviceRepository: DeviceRepository) =
        TokenAuthentication(accessTokenStore, deviceRepository, clock)
}
