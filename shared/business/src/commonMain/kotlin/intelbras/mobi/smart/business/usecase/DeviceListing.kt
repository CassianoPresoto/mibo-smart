package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.business.session.rejectsTheAccessToken
import intelbras.mobi.smart.domain.device.DeviceRepository
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import kotlin.coroutines.cancellation.CancellationException

internal class DeviceListing(
    private val deviceRepository: DeviceRepository,
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

    private fun DeviceListPage.toResult(): DeviceListResult =
        if (isEmpty && page == DeviceListQuery.FIRST_PAGE) {
            DeviceListResult.Empty
        } else {
            DeviceListResult.Success(this)
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
