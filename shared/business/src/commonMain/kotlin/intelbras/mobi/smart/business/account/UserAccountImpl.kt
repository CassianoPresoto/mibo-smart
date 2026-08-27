package intelbras.mobi.smart.business.account

import intelbras.mobi.smart.business.account.usecase.AccountInspection
import intelbras.mobi.smart.business.account.usecase.AccountSummaryResult

internal class UserAccountImpl(
    private val accountInspection: AccountInspection,
) : UserAccount {

    override suspend fun summary(): AccountSummaryResult = accountInspection()
}
