package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.camera.CameraRepository
import intelbras.mobi.smart.domain.camera.model.RecordingPlayback
import intelbras.mobi.smart.domain.camera.model.RecordingRequest
import intelbras.mobi.smart.domain.camera.model.VideoStream
import intelbras.mobi.smart.domain.camera.model.VideoStreamRequest
import intelbras.mobi.smart.rest.SmartHomeDeviceOfflineException
import intelbras.mobi.smart.rest.SmartHomeRecordingNotFoundException
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.PlatformErrorHandler
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller

private const val BAD_REQUEST = 400
private const val OFFLINE_MARKER = "offline"
private const val NO_VIDEO_MARKER = "nenhum vídeo"

private val recordingErrors: PlatformErrorHandler = { status, message ->
    if (status == BAD_REQUEST) {
        when {
            message.contains(OFFLINE_MARKER, ignoreCase = true) ->
                throw SmartHomeDeviceOfflineException(message)

            message.contains(NO_VIDEO_MARKER, ignoreCase = true) ->
                throw SmartHomeRecordingNotFoundException(message)
        }
    }
}

internal class CameraRestRepository(
    private val caller: SmartHomeApiCaller,
) : CameraRepository {

    override suspend fun openVideoStream(request: VideoStreamRequest): VideoStream =
        caller.query(
            route = ApiRoutes.CREATE_VIDEO_STREAM,
            body = request,
            bodySerializer = VideoStreamRequest.serializer(),
            responseDeserializer = VideoStream.serializer(),
        )

    override suspend fun loadRecording(request: RecordingRequest): RecordingPlayback =
        caller.query(
            route = ApiRoutes.RECORDING,
            body = request,
            bodySerializer = RecordingRequest.serializer(),
            responseDeserializer = RecordingPlayback.serializer(),
            onEndpointError = recordingErrors,
        )
}
