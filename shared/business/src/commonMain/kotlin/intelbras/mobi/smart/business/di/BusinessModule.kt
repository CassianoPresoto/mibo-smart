package intelbras.mobi.smart.business.di

import intelbras.mobi.smart.business.DeviceCatalog
import intelbras.mobi.smart.business.DeviceCatalogImpl
import intelbras.mobi.smart.business.DeviceConnector
import intelbras.mobi.smart.business.DeviceConnectorImpl
import intelbras.mobi.smart.business.LockController
import intelbras.mobi.smart.business.LockControllerImpl
import intelbras.mobi.smart.business.SmartHomeSession
import intelbras.mobi.smart.business.SmartHomeSessionImpl
import intelbras.mobi.smart.business.StreamingMonitor
import intelbras.mobi.smart.business.StreamingMonitorImpl
import intelbras.mobi.smart.business.ThemeSettings
import intelbras.mobi.smart.business.ThemeSettingsImpl
import intelbras.mobi.smart.business.UserAccount
import intelbras.mobi.smart.business.UserAccountImpl
import intelbras.mobi.smart.business.VideoPlayback
import intelbras.mobi.smart.business.VideoPlaybackImpl
import intelbras.mobi.smart.business.session.StoredAccessTokenProvider
import intelbras.mobi.smart.business.usecase.AccountInspection
import intelbras.mobi.smart.business.usecase.ConnectionTermination
import intelbras.mobi.smart.business.usecase.DeviceConnecting
import intelbras.mobi.smart.business.usecase.DeviceKindResolution
import intelbras.mobi.smart.business.usecase.DeviceListing
import intelbras.mobi.smart.business.usecase.LiveVideoPlayback
import intelbras.mobi.smart.business.usecase.LockConfirmationPolicy
import intelbras.mobi.smart.business.usecase.LockInspection
import intelbras.mobi.smart.business.usecase.LockSwitching
import intelbras.mobi.smart.business.usecase.PlaybackRetryPolicy
import intelbras.mobi.smart.business.usecase.SessionInspection
import intelbras.mobi.smart.business.usecase.StreamingUsageReading
import intelbras.mobi.smart.business.usecase.SessionTermination
import intelbras.mobi.smart.business.usecase.TokenAuthentication
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
    single { LockConfirmationPolicy() }
    single { PlaybackRetryPolicy() }
    single<AccessTokenProvider> { StoredAccessTokenProvider(get(), get()) }

    factory { AccountInspection(get(), get()) }
    factory { ConnectionTermination(get()) }
    factory { DeviceConnecting(get(), get()) }
    factory { DeviceKindResolution(get(), get()) }
    factory { DeviceListing(get(), get()) }
    factory { LiveVideoPlayback(get(), get()) }
    factory { LockInspection(get()) }
    factory { LockSwitching(get(), get()) }
    factory { SessionInspection(get(), get()) }
    factory { SessionTermination(get()) }
    factory { StreamingUsageReading(get()) }
    factory { TokenAuthentication(get(), get(), get()) }

    single<DeviceCatalog> { DeviceCatalogImpl(get()) }
    single<DeviceConnector> { DeviceConnectorImpl(get(), get()) }
    single<LockController> { LockControllerImpl(get(), get()) }
    single<SmartHomeSession> { SmartHomeSessionImpl(get(), get(), get()) }
    single<StreamingMonitor> { StreamingMonitorImpl(get()) }
    single<ThemeSettings> { ThemeSettingsImpl(get()) }
    single<UserAccount> { UserAccountImpl(get()) }
    single<VideoPlayback> { VideoPlaybackImpl(get()) }
}
