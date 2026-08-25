package intelbras.mobi.smart.di

import intelbras.mobi.smart.business.di.businessModule
import intelbras.mobi.smart.persistence.SmartHomePersistenceFactory
import intelbras.mobi.smart.ui.feature.devices.DeviceListViewModel
import intelbras.mobi.smart.ui.feature.lock.LockViewModel
import intelbras.mobi.smart.ui.feature.session.SessionViewModel
import intelbras.mobi.smart.ui.feature.token.TokenEntryViewModel
import intelbras.mobi.smart.ui.feature.video.LiveVideoViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun startSmartHomeDependencies(
    persistenceFactory: SmartHomePersistenceFactory,
    logNetworkTraffic: Boolean = false,
) {
    startKoin {
        modules(businessModule(persistenceFactory, logNetworkTraffic), presentationModule())
    }
}

private fun presentationModule(): Module = module {
    viewModelOf(::LockViewModel)
    viewModelOf(::SessionViewModel)
    viewModelOf(::TokenEntryViewModel)
    viewModelOf(::DeviceListViewModel)
    viewModelOf(::LiveVideoViewModel)
}
