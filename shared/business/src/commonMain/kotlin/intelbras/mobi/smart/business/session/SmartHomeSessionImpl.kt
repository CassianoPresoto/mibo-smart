package intelbras.mobi.smart.business.session

import intelbras.mobi.smart.business.token.usecase.AuthenticationResult
import intelbras.mobi.smart.business.session.usecase.SessionInspection
import intelbras.mobi.smart.business.session.usecase.SessionStatus
import intelbras.mobi.smart.business.session.usecase.SessionTermination
import intelbras.mobi.smart.business.token.usecase.TokenAuthentication

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
