package intelbras.mobi.smart.business.activity.usecase

import intelbras.mobi.smart.business.lock.usecase.LockOpening

data class HomeActivityEntry(
    val lockName: String,
    val opening: LockOpening,
)

sealed interface HomeActivityResult {
    data class Loaded(val entries: List<HomeActivityEntry>) : HomeActivityResult

    data object NoLocks : HomeActivityResult

    data object Unavailable : HomeActivityResult

    data object InvalidToken : HomeActivityResult

    data object NetworkUnavailable : HomeActivityResult

    data class Error(val cause: Throwable) : HomeActivityResult
}
