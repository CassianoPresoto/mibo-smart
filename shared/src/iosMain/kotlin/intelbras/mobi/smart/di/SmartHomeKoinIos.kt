package intelbras.mobi.smart.di

import intelbras.mobi.smart.persistence.NativePersistenceFactory

fun startSmartHomeDependencies(logNetworkTraffic: Boolean = false) =
    startSmartHomeDependencies(NativePersistenceFactory(), logNetworkTraffic)
