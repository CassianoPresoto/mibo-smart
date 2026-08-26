package intelbras.mobi.smart.business.usecase

sealed interface AccountSummaryResult {
    data class Success(val account: AccountSummary) : AccountSummaryResult

    data object SessionMissing : AccountSummaryResult
}
