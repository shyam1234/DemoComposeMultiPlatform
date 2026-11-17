package com.ott.platform.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val databaseIoDispatcher: CoroutineDispatcher = Dispatchers.IO

internal actual fun createDatabaseModule(): Module = module {
    single<SqlDriver> {
        NativeSqliteDriver(
            schema = OttDatabase.Schema,
            name = OTT_DATABASE_NAME,
        )
    }
    single { OttDatabase(driver = get()) }
    single<CatalogLocalDataSource> { SqlDelightCatalogLocalDataSource(database = get()) }
    single<WatchlistLocalDataSource> { SqlDelightWatchlistLocalDataSource(database = get()) }
}
