package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionTermination
import intelbras.mobi.smart.business.usecase.TokenAuthentication

internal class SmartHomeSessionImpl(
    private val tokenAuthentication: TokenAuthentication,
    private val sessionTermination: SessionTermination,
) : SmartHomeSession {

    override suspend fun authenticate(accessToken: String): AuthenticationResult =
        tokenAuthentication(accessToken)

    override suspend fun signOut() = sessionTermination()
}
