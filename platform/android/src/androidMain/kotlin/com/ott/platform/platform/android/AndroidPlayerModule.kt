package com.ott.platform.platform.android

import com.ott.platform.domain.playback.PlayerEngine
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlayerModule = module {
    single<PlayerEngine> { Media3PlayerEngine(androidContext()) }
}
