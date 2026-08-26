package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.DeviceKind
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class HomeActivityReading(
    private val deviceListing: DeviceListing,
    private val lockHistoryReading: LockHistoryReading,
) {

    suspend operator fun invoke(limitPerLock: Int): HomeActivityResult =
        when (val devices = deviceListing()) {
            is DeviceListResult.Success -> devices.devices.locks().toActivity(limitPerLock)
            DeviceListResult.Empty -> HomeActivityResult.NoLocks
            DeviceListResult.InvalidToken -> HomeActivityResult.InvalidToken
            DeviceListResult.NetworkUnavailable -> HomeActivityResult.NetworkUnavailable
            is DeviceListResult.Error -> HomeActivityResult.Error(devices.cause)
        }

    private fun List<CatalogDevice>.locks() = filter { device -> device.kind == DeviceKind.Lock }

    private suspend fun List<CatalogDevice>.toActivity(limitPerLock: Int): HomeActivityResult {
        if (isEmpty()) return HomeActivityResult.NoLocks

        val histories = coroutineScope {
            map { lock -> async { lock to lockHistoryReading(lock.device.reference(), limitPerLock) } }
                .awaitAll()
        }
        return histories.toResult()
    }

    private fun List<Pair<CatalogDevice, LockHistoryResult>>.toResult(): HomeActivityResult {
        val entries = flatMap { (lock, history) -> history.entriesOf(lock) }
        if (entries.isNotEmpty()) return HomeActivityResult.Loaded(entries.mostRecentFirst())

        return firstFailure() ?: HomeActivityResult.Loaded(emptyList())
    }

    private fun LockHistoryResult.entriesOf(lock: CatalogDevice): List<HomeActivityEntry> =
        if (this is LockHistoryResult.Loaded) {
            openings.map { opening -> HomeActivityEntry(lock.displayName(), opening) }
        } else {
            emptyList()
        }

    private fun List<Pair<CatalogDevice, LockHistoryResult>>.firstFailure(): HomeActivityResult? =
        map { (_, history) -> history }.firstNotNullOfOrNull { history -> history.toFailure() }

    private fun LockHistoryResult.toFailure(): HomeActivityResult? = when (this) {
        LockHistoryResult.Unavailable -> HomeActivityResult.Unavailable
        LockHistoryResult.InvalidToken -> HomeActivityResult.InvalidToken
        LockHistoryResult.NetworkUnavailable -> HomeActivityResult.NetworkUnavailable
        LockHistoryResult.DeviceOffline -> HomeActivityResult.Unavailable
        is LockHistoryResult.Error -> HomeActivityResult.Error(cause)
        is LockHistoryResult.Loaded -> null
    }

    private fun List<HomeActivityEntry>.mostRecentFirst() =
        sortedByDescending { entry -> entry.opening.happenedAt }

    private fun CatalogDevice.displayName() =
        device.name.ifBlank { device.model }.ifBlank { device.serialNumber }
}
