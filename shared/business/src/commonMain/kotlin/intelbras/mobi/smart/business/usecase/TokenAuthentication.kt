package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.domain.auth.model.AccessToken
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock

internal class TokenAuthentication(
    private val accessTokenStore: AccessTokenStore,
    private val deviceRepository: DeviceRepository,
    private val clock: Clock,
) {

    suspend operator fun invoke(accessToken: String): AuthenticationResult {
        val sanitizedToken = accessToken.trim()
        if (sanitizedToken.isEmpty()) return AuthenticationResult.MissingToken

        val issuedToken = AccessToken.issuedAt(value = sanitizedToken, issuedAt = clock.now())
        accessTokenStore.save(issuedToken)

        return try {
            deviceRepository.listDevices(VALIDATION_QUERY)
            AuthenticationResult.Success(expiresAt = issuedToken.expiresAt)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            accessTokenStore.clear()
            failure.toAuthenticationResult()
        }
    }

    private fun Throwable.toAuthenticationResult(): AuthenticationResult = when {
        rejectsTheAccessToken() -> AuthenticationResult.InvalidToken
        this is SmartHomeUnknownPlatformErrorException -> AuthenticationResult.InvalidToken
        this is SmartHomeNetworkException -> AuthenticationResult.NetworkUnavailable
        else -> AuthenticationResult.Error(this)
    }

    private companion object {
        val VALIDATION_QUERY = DeviceListQuery(
            page = DeviceListQuery.FIRST_PAGE,
            pageSize = 1,
        )
    }
}
