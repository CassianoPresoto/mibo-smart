package intelbras.mobi.smart.domain.lock

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.DynamicPasswordRequest
import intelbras.mobi.smart.domain.lock.model.LockControlRequest
import intelbras.mobi.smart.domain.lock.model.LockHistoryRequest
import intelbras.mobi.smart.domain.lock.model.LockOpeningRecord
import intelbras.mobi.smart.domain.lock.model.LockOpeningStatus
import intelbras.mobi.smart.domain.lock.model.LockVolumeRequest
import intelbras.mobi.smart.domain.lock.model.LockVolumeStatus
import intelbras.mobi.smart.domain.lock.model.PasswordDeletionRequest
import intelbras.mobi.smart.domain.lock.model.PeriodicPasswordRequest
import intelbras.mobi.smart.domain.lock.model.RemoteOpeningRequest
import intelbras.mobi.smart.domain.lock.model.RemoteOpeningStatus
import intelbras.mobi.smart.domain.lock.model.SinglePasswordRequest

interface LockRepository {
    suspend fun readOpeningStatus(reference: DeviceReference): LockOpeningStatus

    suspend fun control(request: LockControlRequest)

    suspend fun readVolume(reference: DeviceReference): LockVolumeStatus

    suspend fun changeVolume(request: LockVolumeRequest)

    suspend fun readOpeningHistory(request: LockHistoryRequest): List<LockOpeningRecord>

    suspend fun readRemoteOpeningStatus(reference: DeviceReference): RemoteOpeningStatus

    suspend fun enableRemoteOpening(request: RemoteOpeningRequest)

    suspend fun createSinglePassword(request: SinglePasswordRequest)

    suspend fun createPeriodicPassword(request: PeriodicPasswordRequest)

    suspend fun createDynamicPassword(request: DynamicPasswordRequest)

    suspend fun deleteSinglePassword(request: PasswordDeletionRequest)

    suspend fun deletePeriodicPassword(request: PasswordDeletionRequest)
}
