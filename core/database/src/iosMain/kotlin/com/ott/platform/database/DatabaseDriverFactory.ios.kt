package com.ott.platform.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
    single<SqlDriver> {
        NativeSqliteDriver(
            schema = OttDatabase.Schema,
            name = OTT_DATABASE_NAME,
        )
    }
}
