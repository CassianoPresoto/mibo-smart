package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import kotlin.coroutines.cancellation.CancellationException

internal class TokenAuthentication(
    private val accessTokenStore: AccessTokenStore,
    private val deviceRepository: DeviceRepository,
) {

    suspend operator fun invoke(accessToken: String): AuthenticationResult {
        val sanitizedToken = accessToken.trim()
        if (sanitizedToken.isEmpty()) return AuthenticationResult.MissingToken

        accessTokenStore.save(sanitizedToken)
        return try {
            deviceRepository.listDevices(VALIDATION_QUERY)
            AuthenticationResult.Success
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            accessTokenStore.clear()
            failure.toAuthenticationResult()
        }
    }

    private fun Throwable.toAuthenticationResult(): AuthenticationResult = when {
        rejectsTheAccessToken() -> AuthenticationResult.InvalidToken
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
