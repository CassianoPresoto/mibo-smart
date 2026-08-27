package intelbras.mobi.smart.business.device

import intelbras.mobi.smart.business.device.usecase.DeviceListResult
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter

interface DeviceCatalog {
    suspend fun listDevices(
        origin: DeviceOriginFilter = DeviceOriginFilter.All,
        page: Int = DeviceListQuery.FIRST_PAGE,
        pageSize: Int = DeviceListQuery.DEFAULT_PAGE_SIZE,
    ): DeviceListResult
}
