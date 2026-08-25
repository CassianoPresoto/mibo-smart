package intelbras.mobi.smart.di

import intelbras.mobi.smart.business.di.businessModule
import org.koin.core.context.startKoin

fun startSmartHomeDependencies(logNetworkTraffic: Boolean = false) {
    startKoin {
        modules(businessModule(logNetworkTraffic))
    }
}
