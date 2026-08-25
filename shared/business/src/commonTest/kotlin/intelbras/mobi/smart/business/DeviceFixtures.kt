package intelbras.mobi.smart.business

import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.lock.LockRepository
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.domain.device.model.DeviceListQuery

internal fun emptyPage(query: DeviceListQuery = DeviceListQuery()) = DeviceListPage(
    page = query.page,
    pageSize = query.pageSize,
    origin = query.origin,
    devices = emptyList(),
)

internal fun noLock() = mock<LockRepository> {
    everySuspend { readOpeningStatus(any()) } throws SmartHomeNotFoundException("HTTP 404")
}
