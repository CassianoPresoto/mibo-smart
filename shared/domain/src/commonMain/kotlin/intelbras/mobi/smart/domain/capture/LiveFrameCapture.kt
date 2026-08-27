package intelbras.mobi.smart.domain.capture

import intelbras.mobi.smart.domain.capture.model.FrameCaptureResult
import intelbras.mobi.smart.domain.capture.model.MediaFileDestination

interface LiveFrameCapture {
    suspend fun captureFrame(destination: MediaFileDestination): FrameCaptureResult
}
