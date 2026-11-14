package com.ott.platform.shared

import com.ott.platform.di.coreModules
import com.ott.platform.feature.details.impl.detailsModule
import com.ott.platform.feature.home.impl.homeModule
import com.ott.platform.feature.player.impl.playerFeatureModule
import com.ott.platform.feature.search.impl.searchModule
import com.ott.platform.feature.watchlist.impl.watchlistModule
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin(extraModules: List<Module> = emptyList()) {
    startKoin {
        allowOverride(true)
        modules(coreModules() + featureModules() + extraModules)
    }
}

fun featureModules(): List<Module> = listOf(
    homeModule,
    searchModule,
    detailsModule,
    playerFeatureModule,
    watchlistModule,
)
