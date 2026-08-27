package intelbras.mobi.smart.business.session

import intelbras.mobi.smart.business.token.usecase.AuthenticationResult
import intelbras.mobi.smart.business.session.usecase.SessionStatus

interface SmartHomeSession {
    suspend fun authenticate(accessToken: String): AuthenticationResult

    suspend fun currentStatus(): SessionStatus

    suspend fun signOut()
}
