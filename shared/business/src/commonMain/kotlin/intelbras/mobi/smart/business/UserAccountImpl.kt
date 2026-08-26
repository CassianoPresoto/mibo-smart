package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.AccountInspection
import intelbras.mobi.smart.business.usecase.AccountSummaryResult

internal class UserAccountImpl(
    private val accountInspection: AccountInspection,
) : UserAccount {

    override suspend fun summary(): AccountSummaryResult = accountInspection()
}
