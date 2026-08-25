package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionStatus

interface SmartHomeSession {
    suspend fun authenticate(accessToken: String): AuthenticationResult

    suspend fun currentStatus(): SessionStatus

    suspend fun signOut()
}
