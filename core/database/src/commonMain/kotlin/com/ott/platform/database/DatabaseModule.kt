package com.ott.platform.database

import org.koin.core.module.Module
import org.koin.dsl.module

internal const val OTT_DATABASE_NAME = "ott_platform.db"

expect fun platformDatabaseModule(): Module

val coreDatabaseModule: Module = module {
    includes(platformDatabaseModule())
    single { OttDatabase(driver = get()) }
    single<CatalogLocalDataSource> { SqlDelightCatalogLocalDataSource(database = get()) }
    single<WatchlistLocalDataSource> { SqlDelightWatchlistLocalDataSource(database = get()) }
}
