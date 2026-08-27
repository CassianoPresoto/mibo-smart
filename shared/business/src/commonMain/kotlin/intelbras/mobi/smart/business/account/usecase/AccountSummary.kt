package intelbras.mobi.smart.business.account.usecase

import kotlin.time.Duration

data class AccountSummary(
    val tokenSuffix: String,
    val expiresIn: Duration,
)
