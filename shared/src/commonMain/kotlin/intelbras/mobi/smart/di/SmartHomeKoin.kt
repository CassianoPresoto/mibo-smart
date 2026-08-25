package intelbras.mobi.smart.di

import intelbras.mobi.smart.business.di.businessModule
import intelbras.mobi.smart.persistence.SmartHomePersistenceFactory
import intelbras.mobi.smart.ui.devices.DeviceListViewModel
import intelbras.mobi.smart.ui.token.TokenEntryViewModel
import intelbras.mobi.smart.ui.video.LiveVideoViewModel
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
    viewModelOf(::TokenEntryViewModel)
    viewModelOf(::DeviceListViewModel)
    viewModelOf(::LiveVideoViewModel)
}
