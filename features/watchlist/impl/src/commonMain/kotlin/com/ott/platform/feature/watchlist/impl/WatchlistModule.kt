package com.ott.platform.feature.watchlist.impl

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val watchlistModule = module {
    viewModelOf(::WatchlistViewModel)
}
