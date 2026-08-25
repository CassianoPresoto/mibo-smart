package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionInspection
import intelbras.mobi.smart.business.usecase.SessionStatus
import intelbras.mobi.smart.business.usecase.SessionTermination
import intelbras.mobi.smart.business.usecase.TokenAuthentication

internal class SmartHomeSessionImpl(
    private val tokenAuthentication: TokenAuthentication,
    private val sessionInspection: SessionInspection,
    private val sessionTermination: SessionTermination,
) : SmartHomeSession {

    override suspend fun authenticate(accessToken: String): AuthenticationResult =
        tokenAuthentication(accessToken)

    override suspend fun currentStatus(): SessionStatus = sessionInspection()

    override suspend fun signOut() = sessionTermination()
}
