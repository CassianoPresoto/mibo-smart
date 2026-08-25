package intelbras.mobi.smart.business

import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.domain.device.model.DeviceReference

interface LockController {
    suspend fun statusOf(lock: DeviceReference): LockStatusResult

    suspend fun switch(lock: DeviceReference, open: Boolean): LockOperationResult
}
