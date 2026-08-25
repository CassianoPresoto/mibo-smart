package intelbras.mobi.smart.di

import intelbras.mobi.smart.business.di.businessModule
import intelbras.mobi.smart.persistence.SmartHomePersistenceFactory
import org.koin.core.context.startKoin

fun startSmartHomeDependencies(
    persistenceFactory: SmartHomePersistenceFactory,
    logNetworkTraffic: Boolean = false,
) {
    startKoin {
        modules(businessModule(persistenceFactory, logNetworkTraffic))
    }
}
