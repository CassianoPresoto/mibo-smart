package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.AccountSummaryResult

interface UserAccount {
    suspend fun summary(): AccountSummaryResult
}
