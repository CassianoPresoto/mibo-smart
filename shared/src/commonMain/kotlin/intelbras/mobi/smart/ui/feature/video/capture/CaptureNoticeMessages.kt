package intelbras.mobi.smart.ui.feature.video.capture

import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_notice_clip_saved
import mibosmart.shared.generated.resources.live_video_notice_failed
import mibosmart.shared.generated.resources.live_video_notice_frame_unavailable
import mibosmart.shared.generated.resources.live_video_notice_nothing_recorded
import mibosmart.shared.generated.resources.live_video_notice_photo_saved
import mibosmart.shared.generated.resources.live_video_notice_recording_unsupported
import org.jetbrains.compose.resources.StringResource

internal fun CaptureNotice.messageResource(): StringResource = when (this) {
    CaptureNotice.PhotoSaved -> Res.string.live_video_notice_photo_saved
    CaptureNotice.ClipSaved -> Res.string.live_video_notice_clip_saved
    CaptureNotice.FrameUnavailable -> Res.string.live_video_notice_frame_unavailable
    CaptureNotice.RecordingUnsupported -> Res.string.live_video_notice_recording_unsupported
    CaptureNotice.NothingRecorded -> Res.string.live_video_notice_nothing_recorded
    CaptureNotice.Failed -> Res.string.live_video_notice_failed
}
