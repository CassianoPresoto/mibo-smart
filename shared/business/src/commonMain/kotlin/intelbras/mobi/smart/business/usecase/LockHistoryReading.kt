package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.domain.lock.model.LockHistoryRequest
import intelbras.mobi.smart.domain.lock.model.LockOpeningRecord
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

private const val REMOTE_APP_WAY = "usuarioRemoto"

private val platformTimeFormat = LocalDateTime.Format {
    year()
    monthNumber()
    day()
    char('T')
    hour()
    minute()
    second()
}

internal class LockHistoryReading(
    private val lockRepository: LockRepository,
) {

    suspend operator fun invoke(lock: DeviceReference, limit: Int): LockHistoryResult = try {
        LockHistoryResult.Loaded(lockRepository.readOpeningHistory(lock.toRequest(limit)).toOpenings())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        failure.toHistoryResult()
    }

    private fun DeviceReference.toRequest(limit: Int) = LockHistoryRequest(
        serialNumber = serialNumber,
        limit = limit,
    )

    private fun List<LockOpeningRecord>.toOpenings() = map { record ->
        LockOpening(
            happenedAt = record.localTime.toLocalDateTimeOrNull(),
            user = record.user,
            way = record.way.toWay(),
        )
    }

    private fun String.toLocalDateTimeOrNull(): LocalDateTime? = try {
        LocalDateTime.parse(this, platformTimeFormat)
    } catch (unreadableTime: IllegalArgumentException) {
        null
    }

    private fun String.toWay(): LockOpeningWay =
        if (this == REMOTE_APP_WAY) LockOpeningWay.RemoteApp else LockOpeningWay.Unrecognized(this)

    private fun Throwable.toHistoryResult(): LockHistoryResult = when (asLockFailureKind()) {
        LockFailureKind.DeviceOffline -> LockHistoryResult.DeviceOffline
        LockFailureKind.InvalidToken -> LockHistoryResult.InvalidToken
        LockFailureKind.NetworkUnavailable -> LockHistoryResult.NetworkUnavailable
        LockFailureKind.PlatformFailure -> LockHistoryResult.Unavailable
        LockFailureKind.Refused, LockFailureKind.Unexpected -> LockHistoryResult.Error(this)
    }
}
