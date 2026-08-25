package intelbras.mobi.smart

import android.app.Application
import intelbras.mobi.smart.di.startSmartHomeDependencies

class MiboSmartApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startSmartHomeDependencies(logNetworkTraffic = BuildConfig.DEBUG)
    }
}
