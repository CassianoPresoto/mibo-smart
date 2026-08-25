package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.auth.AuthenticationRepository
import intelbras.mobi.smart.domain.auth.model.RenewedAccessToken
import intelbras.mobi.smart.domain.auth.model.TokenRenewalRequest
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller

internal class AuthenticationRestRepository(
    private val caller: SmartHomeApiCaller,
) : AuthenticationRepository {

    override suspend fun renewToken(request: TokenRenewalRequest): RenewedAccessToken =
        caller.query(
            route = ApiRoutes.RENEW_TOKEN,
            body = request,
            bodySerializer = TokenRenewalRequest.serializer(),
            responseDeserializer = RenewedAccessToken.serializer(),
        )
}
