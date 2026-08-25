package intelbras.mobi.smart.ui.feature.video

import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.live_video_failure_device_offline
import mibosmart.shared.generated.resources.live_video_headline_device_offline
import mibosmart.shared.generated.resources.live_video_headline_interrupted
import mibosmart.shared.generated.resources.live_video_headline_network
import mibosmart.shared.generated.resources.live_video_headline_not_supported
import mibosmart.shared.generated.resources.live_video_headline_quota
import mibosmart.shared.generated.resources.live_video_headline_session_expired
import mibosmart.shared.generated.resources.live_video_headline_unexpected
import mibosmart.shared.generated.resources.live_video_failure_interrupted
import mibosmart.shared.generated.resources.live_video_failure_network
import mibosmart.shared.generated.resources.live_video_failure_not_supported
import mibosmart.shared.generated.resources.live_video_failure_quota
import mibosmart.shared.generated.resources.live_video_failure_session_expired
import mibosmart.shared.generated.resources.live_video_failure_unexpected
import org.jetbrains.compose.resources.StringResource

internal fun LiveVideoFailure.messageResource(): StringResource = when (this) {
    LiveVideoFailure.NotSupported -> Res.string.live_video_failure_not_supported
    LiveVideoFailure.DeviceOffline -> Res.string.live_video_failure_device_offline
    LiveVideoFailure.QuotaExceeded -> Res.string.live_video_failure_quota
    LiveVideoFailure.SessionExpired -> Res.string.live_video_failure_session_expired
    LiveVideoFailure.NetworkUnavailable -> Res.string.live_video_failure_network
    LiveVideoFailure.PlaybackInterrupted -> Res.string.live_video_failure_interrupted
    LiveVideoFailure.Unexpected -> Res.string.live_video_failure_unexpected
}

internal fun LiveVideoFailure.headlineResource(): StringResource = when (this) {
    LiveVideoFailure.NotSupported -> Res.string.live_video_headline_not_supported
    LiveVideoFailure.DeviceOffline -> Res.string.live_video_headline_device_offline
    LiveVideoFailure.QuotaExceeded -> Res.string.live_video_headline_quota
    LiveVideoFailure.SessionExpired -> Res.string.live_video_headline_session_expired
    LiveVideoFailure.NetworkUnavailable -> Res.string.live_video_headline_network
    LiveVideoFailure.PlaybackInterrupted -> Res.string.live_video_headline_interrupted
    LiveVideoFailure.Unexpected -> Res.string.live_video_headline_unexpected
}
