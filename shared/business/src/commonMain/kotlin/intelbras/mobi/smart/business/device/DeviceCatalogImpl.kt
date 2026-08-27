package intelbras.mobi.smart.business.device

import intelbras.mobi.smart.business.device.usecase.DeviceListResult
import intelbras.mobi.smart.business.device.usecase.DeviceListing
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter

internal class DeviceCatalogImpl(
    private val deviceListing: DeviceListing,
) : DeviceCatalog {

    override suspend fun listDevices(
        origin: DeviceOriginFilter,
        page: Int,
        pageSize: Int,
    ): DeviceListResult = deviceListing(origin = origin, page = page, pageSize = pageSize)
}
