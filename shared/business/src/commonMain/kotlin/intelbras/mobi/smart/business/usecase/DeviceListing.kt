package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

internal class DeviceListing(
    private val deviceRepository: DeviceRepository,
    private val deviceKindResolution: DeviceKindResolution,
) {

    suspend operator fun invoke(
        origin: DeviceOriginFilter = DeviceOriginFilter.All,
        page: Int = DeviceListQuery.FIRST_PAGE,
        pageSize: Int = DeviceListQuery.DEFAULT_PAGE_SIZE,
    ): DeviceListResult {
        val query = DeviceListQuery(
            page = page.coerceAtLeast(DeviceListQuery.FIRST_PAGE),
            pageSize = pageSize.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE),
            origin = origin,
        )
        return try {
            deviceRepository.listDevices(query).toResult()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Throwable) {
            failure.toResult()
        }
    }

    private suspend fun DeviceListPage.toResult(): DeviceListResult =
        if (isEmpty && page == DeviceListQuery.FIRST_PAGE) {
            DeviceListResult.Empty
        } else {
            DeviceListResult.Success(devices.withTheirKinds())
        }

    private suspend fun List<Device>.withTheirKinds(): List<CatalogDevice> = coroutineScope {
        val hubs = hubsOfThePage()
        map { device -> async { device.toCatalogDevice(hubs) } }.awaitAll()
    }

    private fun List<Device>.hubsOfThePage(): Set<String> =
        mapNotNull { device -> device.hubSerialNumber.ifBlank { null } }.toSet()

    private suspend fun Device.toCatalogDevice(hubs: Set<String>) = CatalogDevice(
        device = this,
        kind = if (serialNumber in hubs) DeviceKind.Hub else kindOrUnknown(),
    )

    private suspend fun Device.kindOrUnknown(): DeviceKind = try {
        deviceKindResolution(this)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unreadableCapabilities: Throwable) {
        DeviceKind.Unknown
    }

    private fun Throwable.toResult(): DeviceListResult = when {
        rejectsTheAccessToken() -> DeviceListResult.InvalidToken
        this is SmartHomeNetworkException -> DeviceListResult.NetworkUnavailable
        else -> DeviceListResult.Error(this)
    }

    private companion object {
        const val MIN_PAGE_SIZE = 1
        const val MAX_PAGE_SIZE = 100
    }
}
