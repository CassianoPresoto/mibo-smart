package intelbras.mobi.smart.business.di

import intelbras.mobi.smart.business.activity.ActivityFeed
import intelbras.mobi.smart.business.activity.ActivityFeedImpl
import intelbras.mobi.smart.business.capture.CameraCaptures
import intelbras.mobi.smart.business.capture.CameraCapturesImpl
import intelbras.mobi.smart.business.device.DeviceCatalog
import intelbras.mobi.smart.business.device.DeviceCatalogImpl
import intelbras.mobi.smart.business.device.DeviceConnector
import intelbras.mobi.smart.business.device.DeviceConnectorImpl
import intelbras.mobi.smart.business.lock.LockController
import intelbras.mobi.smart.business.lock.LockControllerImpl
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.session.SmartHomeSessionImpl
import intelbras.mobi.smart.business.streaming.StreamingMonitor
import intelbras.mobi.smart.business.streaming.StreamingMonitorImpl
import intelbras.mobi.smart.business.theme.ThemeSettings
import intelbras.mobi.smart.business.theme.ThemeSettingsImpl
import intelbras.mobi.smart.business.account.UserAccount
import intelbras.mobi.smart.business.account.UserAccountImpl
import intelbras.mobi.smart.business.video.VideoPlayback
import intelbras.mobi.smart.business.video.VideoPlaybackImpl
import intelbras.mobi.smart.business.session.StoredAccessTokenProvider
import intelbras.mobi.smart.business.account.usecase.AccountInspection
import intelbras.mobi.smart.business.capture.usecase.CaptureFileNaming
import intelbras.mobi.smart.business.capture.usecase.CaptureLibraryReading
import intelbras.mobi.smart.business.capture.usecase.CaptureMediaReading
import intelbras.mobi.smart.business.capture.usecase.CaptureRemoval
import intelbras.mobi.smart.business.device.usecase.ConnectionTermination
import intelbras.mobi.smart.business.device.usecase.DeviceConnecting
import intelbras.mobi.smart.business.device.usecase.DeviceKindResolution
import intelbras.mobi.smart.business.device.usecase.DeviceListing
import intelbras.mobi.smart.business.activity.usecase.HomeActivityReading
import intelbras.mobi.smart.business.capture.usecase.LiveClipRecording
import intelbras.mobi.smart.business.capture.usecase.LiveSnapshotTaking
import intelbras.mobi.smart.business.video.usecase.LiveVideoPlayback
import intelbras.mobi.smart.business.lock.usecase.LockConfirmation
import intelbras.mobi.smart.business.lock.usecase.LockConfirmationPolicy
import intelbras.mobi.smart.business.lock.usecase.LockDetailsReading
import intelbras.mobi.smart.business.lock.usecase.LockHistoryReading
import intelbras.mobi.smart.business.lock.usecase.LockInspection
import intelbras.mobi.smart.business.lock.usecase.LockSwitching
import intelbras.mobi.smart.business.lock.usecase.LockVolumeChanging
import intelbras.mobi.smart.business.lock.usecase.LockVolumeMemory
import intelbras.mobi.smart.business.lock.usecase.LockVolumeReading
import intelbras.mobi.smart.business.video.usecase.PlaybackRetryPolicy
import intelbras.mobi.smart.business.session.usecase.SessionInspection
import intelbras.mobi.smart.business.streaming.usecase.StreamingUsageReading
import intelbras.mobi.smart.business.session.usecase.SessionTermination
import intelbras.mobi.smart.business.token.usecase.TokenAuthentication
import intelbras.mobi.smart.domain.auth.AccessTokenProvider
import intelbras.mobi.smart.persistence.SmartHomePersistenceFactory
import intelbras.mobi.smart.persistence.di.persistenceModule
import intelbras.mobi.smart.rest.RestConfiguration
import intelbras.mobi.smart.rest.di.restModule
import kotlin.time.Clock
import org.koin.core.module.Module
import org.koin.dsl.module

fun businessModule(
    persistenceFactory: SmartHomePersistenceFactory,
    logNetworkTraffic: Boolean = false,
): Module = module {
    includes(
        persistenceModule(persistenceFactory),
        restModule(RestConfiguration(logRequests = logNetworkTraffic)),
    )

    single<Clock> { Clock.System }
    single { CaptureFileNaming() }
    single { LockConfirmationPolicy() }
    single { PlaybackRetryPolicy() }
    single<AccessTokenProvider> { StoredAccessTokenProvider(get(), get()) }

    factory { AccountInspection(get(), get()) }
    factory { CaptureLibraryReading(get()) }
    factory { CaptureMediaReading(get()) }
    factory { CaptureRemoval(get(), get()) }
    factory { ConnectionTermination(get()) }
    factory { DeviceConnecting(get(), get()) }
    factory { DeviceKindResolution(get(), get()) }
    factory { DeviceListing(get(), get()) }
    factory { HomeActivityReading(get(), get()) }
    factory { LiveClipRecording(get(), get(), get(), get()) }
    factory { LiveSnapshotTaking(get(), get(), get(), get()) }
    factory { LiveVideoPlayback(get(), get()) }
    factory { LockConfirmation(get()) }
    factory { LockDetailsReading(get(), get(), get()) }
    factory { LockHistoryReading(get()) }
    factory { LockInspection(get()) }
    factory { LockSwitching(get(), get()) }
    factory { LockVolumeChanging(get(), get(), get()) }
    factory { LockVolumeMemory(get()) }
    factory { LockVolumeReading(get(), get()) }
    factory { SessionInspection(get(), get()) }
    factory { SessionTermination(get()) }
    factory { StreamingUsageReading(get()) }
    factory { TokenAuthentication(get(), get(), get()) }

    single<ActivityFeed> { ActivityFeedImpl(get()) }
    single<CameraCaptures> { CameraCapturesImpl(get(), get(), get(), get(), get()) }
    single<DeviceCatalog> { DeviceCatalogImpl(get()) }
    single<DeviceConnector> { DeviceConnectorImpl(get(), get()) }
    single<LockController> { LockControllerImpl(get(), get(), get(), get(), get(), get()) }
    single<SmartHomeSession> { SmartHomeSessionImpl(get(), get(), get()) }
    single<StreamingMonitor> { StreamingMonitorImpl(get()) }
    single<ThemeSettings> { ThemeSettingsImpl(get()) }
    single<UserAccount> { UserAccountImpl(get()) }
    single<VideoPlayback> { VideoPlaybackImpl(get()) }
}
