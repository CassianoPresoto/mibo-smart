package intelbras.mobi.smart.business.account.usecase

sealed interface AccountSummaryResult {
    data class Success(val account: AccountSummary) : AccountSummaryResult

    data object SessionMissing : AccountSummaryResult
}
