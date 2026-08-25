package intelbras.mobi.smart.di

import android.content.Context
import intelbras.mobi.smart.persistence.AndroidPersistenceFactory

fun startSmartHomeDependencies(context: Context, logNetworkTraffic: Boolean = false) =
    startSmartHomeDependencies(AndroidPersistenceFactory(context), logNetworkTraffic)
