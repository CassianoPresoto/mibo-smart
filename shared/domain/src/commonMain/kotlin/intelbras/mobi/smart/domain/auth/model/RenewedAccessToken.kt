package intelbras.mobi.smart.domain.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class RenewedAccessToken(val token: String = "")
