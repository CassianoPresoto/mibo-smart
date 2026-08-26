package intelbras.mobi.smart.business.usecase

import kotlin.time.Duration

data class AccountSummary(
    val tokenSuffix: String,
    val expiresIn: Duration,
)
