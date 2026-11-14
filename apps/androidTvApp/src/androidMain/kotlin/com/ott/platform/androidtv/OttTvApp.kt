package com.ott.platform.androidtv

import android.app.Application
import com.ott.platform.di.coreModules
import com.ott.platform.platform.android.androidPlayerModule
import com.ott.platform.shared.featureModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class OttTvApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            allowOverride(true)
            androidLogger(Level.ERROR)
            androidContext(this@OttTvApp)
            modules(coreModules() + featureModules() + listOf(androidPlayerModule))
        }
    }
}
