package intelbras.mobi.smart.business.lock.usecase

import kotlinx.datetime.LocalDateTime

data class LockOpening(
    val happenedAt: LocalDateTime?,
    val user: String,
    val way: LockOpeningWay,
)

sealed interface LockOpeningWay {
    data object RemoteApp : LockOpeningWay

    data class Unrecognized(val name: String) : LockOpeningWay
}
