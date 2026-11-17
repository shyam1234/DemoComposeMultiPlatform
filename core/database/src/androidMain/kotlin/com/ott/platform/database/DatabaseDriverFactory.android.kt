package com.ott.platform.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val databaseIoDispatcher: CoroutineDispatcher = Dispatchers.IO

internal actual fun createDatabaseModule(): Module = module {
    single<SqlDriver> {
        val context: Context = get()
        AndroidSqliteDriver(
            schema = OttDatabase.Schema,
            context = context,
            name = OTT_DATABASE_NAME,
            callback = object : AndroidSqliteDriver.Callback(OttDatabase.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    db.enableWriteAheadLogging()
                }
            },
        )
    }
    single { OttDatabase(driver = get()) }
    single<CatalogLocalDataSource> { SqlDelightCatalogLocalDataSource(database = get()) }
    single<WatchlistLocalDataSource> { SqlDelightWatchlistLocalDataSource(database = get()) }
}
