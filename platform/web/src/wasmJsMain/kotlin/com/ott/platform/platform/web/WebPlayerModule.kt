package com.ott.platform.platform.web

import com.ott.platform.domain.playback.PlayerEngine
import org.koin.dsl.module

val webPlayerModule = module {
    single<PlayerEngine> { WebPlayerEngine() }
}
