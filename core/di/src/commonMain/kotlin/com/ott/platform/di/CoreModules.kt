package com.ott.platform.di

import com.ott.platform.data.iptv.IptvCatalogRemoteDataSource
import com.ott.platform.data.repository.InMemoryWatchlistRepository
import com.ott.platform.data.repository.IptvCatalogRepository
import com.ott.platform.domain.repository.CatalogRepository
import com.ott.platform.domain.repository.WatchlistRepository
import com.ott.platform.domain.usecase.GetChannelDetailsUseCase
import com.ott.platform.domain.usecase.GetHomeFeedUseCase
import com.ott.platform.domain.usecase.SearchChannelsUseCase
import com.ott.platform.domain.usecase.ToggleWatchlistUseCase
import com.ott.platform.logging.OttLogger
import com.ott.platform.logging.StdOutLogger
import com.ott.platform.network.createOttHttpClient
import com.ott.platform.domain.playback.PlayerController
import com.ott.platform.domain.playback.PlayerEngine
import com.ott.platform.testing.FakePlayerEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreLoggingModule = module {
    single<OttLogger> { StdOutLogger }
}

val coreNetworkModule = module {
    single { createOttHttpClient() }
}

val coreDataModule = module {
    singleOf(::IptvCatalogRemoteDataSource)
    singleOf(::IptvCatalogRepository) bind CatalogRepository::class
    singleOf(::InMemoryWatchlistRepository) bind WatchlistRepository::class
}

val coreDomainModule = module {
    factoryOf(::GetHomeFeedUseCase)
    factoryOf(::SearchChannelsUseCase)
    factoryOf(::GetChannelDetailsUseCase)
    factoryOf(::ToggleWatchlistUseCase)
}

/**
 * Demo default: fake engine works on all CMP targets.
 * Android app overrides with Media3 via [platformPlayerModule].
 */
val corePlayerModule = module {
    single<PlayerEngine> { FakePlayerEngine() }
    factory { PlayerController(get()) }
}

fun coreModules(): List<Module> = listOf(
    coreLoggingModule,
    coreNetworkModule,
    coreDataModule,
    coreDomainModule,
    corePlayerModule,
)
