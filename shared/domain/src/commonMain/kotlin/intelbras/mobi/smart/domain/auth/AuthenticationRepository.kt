package intelbras.mobi.smart.domain.auth

import intelbras.mobi.smart.domain.auth.model.RenewedAccessToken
import intelbras.mobi.smart.domain.auth.model.TokenRenewalRequest

interface AuthenticationRepository {
    suspend fun renewToken(request: TokenRenewalRequest): RenewedAccessToken
}
