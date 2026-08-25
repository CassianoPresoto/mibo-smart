package intelbras.mobi.smart.business.di

import intelbras.mobi.smart.business.DeviceCatalog
import intelbras.mobi.smart.business.DeviceCatalogImpl
import intelbras.mobi.smart.business.SmartHomeSession
import intelbras.mobi.smart.business.SmartHomeSessionImpl
import intelbras.mobi.smart.business.session.InMemoryAccessTokenStore
import intelbras.mobi.smart.business.usecase.DeviceListing
import intelbras.mobi.smart.business.usecase.SessionTermination
import intelbras.mobi.smart.business.usecase.TokenAuthentication
import intelbras.mobi.smart.domain.auth.AccessTokenProvider
import intelbras.mobi.smart.domain.auth.AccessTokenStore
import intelbras.mobi.smart.rest.RestConfiguration
import intelbras.mobi.smart.rest.di.restModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun businessModule(logNetworkTraffic: Boolean = false): Module = module {
    includes(restModule(RestConfiguration(logRequests = logNetworkTraffic)))

    single<AccessTokenStore> { InMemoryAccessTokenStore() }
    single<AccessTokenProvider> { get<AccessTokenStore>() }

    factory { DeviceListing(get()) }
    factory { TokenAuthentication(get(), get()) }
    factory { SessionTermination(get()) }

    single<DeviceCatalog> { DeviceCatalogImpl(get()) }
    single<SmartHomeSession> { SmartHomeSessionImpl(get(), get()) }
}
