package intelbras.mobi.smart.business

import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery

internal fun emptyPage(query: DeviceListQuery = DeviceListQuery()) = DeviceListPage(
    page = query.page,
    pageSize = query.pageSize,
    origin = query.origin,
    devices = emptyList(),
)
