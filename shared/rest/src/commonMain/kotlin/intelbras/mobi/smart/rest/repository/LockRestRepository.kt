package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.LockRepository
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
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller
import kotlinx.serialization.builtins.ListSerializer

internal class LockRestRepository(
    private val caller: SmartHomeApiCaller,
) : LockRepository {

    override suspend fun readOpeningStatus(reference: DeviceReference): LockOpeningStatus =
        caller.query(
            route = ApiRoutes.LOCK_OPENING_STATUS,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = LockOpeningStatus.serializer(),
        )

    override suspend fun control(request: LockControlRequest) =
        caller.command(
            route = ApiRoutes.LOCK_CONTROL,
            body = request,
            bodySerializer = LockControlRequest.serializer(),
        )

    override suspend fun readVolume(reference: DeviceReference): LockVolumeStatus =
        caller.query(
            route = ApiRoutes.LOCK_VOLUME,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = LockVolumeStatus.serializer(),
        )

    override suspend fun changeVolume(request: LockVolumeRequest) =
        caller.command(
            route = ApiRoutes.LOCK_CHANGE_VOLUME,
            body = request,
            bodySerializer = LockVolumeRequest.serializer(),
        )

    override suspend fun readOpeningHistory(
        request: LockHistoryRequest,
    ): List<LockOpeningRecord> =
        caller.query(
            route = ApiRoutes.LOCK_OPENING_HISTORY,
            body = request,
            bodySerializer = LockHistoryRequest.serializer(),
            responseDeserializer = ListSerializer(LockOpeningRecord.serializer()),
        )

    override suspend fun readRemoteOpeningStatus(
        reference: DeviceReference,
    ): RemoteOpeningStatus =
        caller.query(
            route = ApiRoutes.LOCK_REMOTE_OPENING_STATUS,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = RemoteOpeningStatus.serializer(),
        )

    override suspend fun enableRemoteOpening(request: RemoteOpeningRequest) =
        caller.command(
            route = ApiRoutes.LOCK_ENABLE_REMOTE_OPENING,
            body = request,
            bodySerializer = RemoteOpeningRequest.serializer(),
        )

    override suspend fun createSinglePassword(request: SinglePasswordRequest) =
        caller.command(
            route = ApiRoutes.LOCK_CREATE_SINGLE_PASSWORD,
            body = request,
            bodySerializer = SinglePasswordRequest.serializer(),
        )

    override suspend fun createPeriodicPassword(request: PeriodicPasswordRequest) =
        caller.command(
            route = ApiRoutes.LOCK_CREATE_PERIODIC_PASSWORD,
            body = request,
            bodySerializer = PeriodicPasswordRequest.serializer(),
        )

    override suspend fun createDynamicPassword(request: DynamicPasswordRequest) =
        caller.command(
            route = ApiRoutes.LOCK_CREATE_DYNAMIC_PASSWORD,
            body = request,
            bodySerializer = DynamicPasswordRequest.serializer(),
        )

    override suspend fun deleteSinglePassword(request: PasswordDeletionRequest) =
        caller.command(
            route = ApiRoutes.LOCK_DELETE_SINGLE_PASSWORD,
            body = request,
            bodySerializer = PasswordDeletionRequest.serializer(),
        )

    override suspend fun deletePeriodicPassword(request: PasswordDeletionRequest) =
        caller.command(
            route = ApiRoutes.LOCK_DELETE_PERIODIC_PASSWORD,
            body = request,
            bodySerializer = PasswordDeletionRequest.serializer(),
        )
}
