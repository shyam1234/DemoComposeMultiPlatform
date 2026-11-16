package com.ott.platform.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
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
}
