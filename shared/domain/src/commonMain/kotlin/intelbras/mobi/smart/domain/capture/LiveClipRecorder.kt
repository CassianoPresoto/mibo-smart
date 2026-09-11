package intelbras.mobi.smart.domain.capture

import intelbras.mobi.smart.domain.capture.model.ClipRecordingOutcome
import intelbras.mobi.smart.domain.capture.model.ClipRecordingStart
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination

interface LiveClipRecorder {
    suspend fun startRecording(destination: MediaFileDestination): ClipRecordingStart

    suspend fun finishRecording(): ClipRecordingOutcome
}
