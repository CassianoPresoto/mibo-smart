package intelbras.mobi.smart.business.account

import intelbras.mobi.smart.business.account.usecase.AccountSummaryResult

interface UserAccount {
    suspend fun summary(): AccountSummaryResult
}
