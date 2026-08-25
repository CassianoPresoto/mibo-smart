package intelbras.mobi.smart.business.di

import intelbras.mobi.smart.business.session.InMemoryAccessTokenStore
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
}
