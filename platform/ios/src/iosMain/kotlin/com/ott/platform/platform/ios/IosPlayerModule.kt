package com.ott.platform.platform.ios

import com.ott.platform.domain.playback.PlayerEngine
import org.koin.dsl.module

val iosPlayerModule = module {
    single<PlayerEngine> { AvPlayerEngine() }
}
