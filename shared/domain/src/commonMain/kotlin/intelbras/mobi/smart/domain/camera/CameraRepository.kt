package intelbras.mobi.smart.domain.camera

import intelbras.mobi.smart.domain.camera.model.RecordingPlayback
import intelbras.mobi.smart.domain.camera.model.RecordingRequest
import intelbras.mobi.smart.domain.camera.model.VideoStream
import intelbras.mobi.smart.domain.camera.model.VideoStreamRequest

interface CameraRepository {
    suspend fun openVideoStream(request: VideoStreamRequest): VideoStream

    suspend fun loadRecording(request: RecordingRequest): RecordingPlayback
}
