package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.AuthenticationResult

interface SmartHomeSession {
    suspend fun authenticate(accessToken: String): AuthenticationResult

    suspend fun signOut()
}
