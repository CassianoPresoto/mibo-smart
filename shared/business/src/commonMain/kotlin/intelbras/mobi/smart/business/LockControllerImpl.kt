package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.LockInspection
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.business.usecase.LockSwitching
import intelbras.mobi.smart.domain.device.model.DeviceReference

internal class LockControllerImpl(
    private val lockInspection: LockInspection,
    private val lockSwitching: LockSwitching,
) : LockController {

    override suspend fun statusOf(lock: DeviceReference): LockStatusResult = lockInspection(lock)

    override suspend fun switch(lock: DeviceReference, open: Boolean): LockOperationResult =
        lockSwitching(lock, open)
}
