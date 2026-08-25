package intelbras.mobi.smart.domain.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class TokenRenewalRequest(val token: String)
