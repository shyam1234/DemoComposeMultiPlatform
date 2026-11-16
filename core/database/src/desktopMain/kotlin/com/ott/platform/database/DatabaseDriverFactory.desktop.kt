package com.ott.platform.database

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.util.Properties
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformDatabaseModule(): Module = module {
    single<SqlDriver> {
        createDesktopSqlDriver()
    }
}

internal fun createDesktopSqlDriver(dbFile: File = defaultDesktopDbFile()): SqlDriver {
    dbFile.parentFile?.mkdirs()
    val driver = JdbcSqliteDriver(
        url = "jdbc:sqlite:${dbFile.absolutePath}",
        properties = Properties().apply {
            put("journal_mode", "WAL")
        },
    )
    ensureSchema(driver)
    return driver
}

private fun defaultDesktopDbFile(): File {
    val home = System.getProperty("user.home").orEmpty().ifBlank { "." }
    return File(File(home, ".ottplatform"), OTT_DATABASE_NAME)
}

internal fun ensureSchema(driver: SqlDriver) {
    val currentVersion = driver.executeQuery(
        identifier = null,
        sql = "PRAGMA user_version;",
        mapper = { cursor ->
            val version = if (cursor.next().value) cursor.getLong(0) ?: 0L else 0L
            QueryResult.Value(version)
        },
        parameters = 0,
    ).value

    val targetVersion = OttDatabase.Schema.version
    if (currentVersion == 0L) {
        OttDatabase.Schema.create(driver)
        driver.execute(null, "PRAGMA user_version = $targetVersion;", 0)
    } else if (currentVersion < targetVersion) {
        OttDatabase.Schema.migrate(driver, currentVersion, targetVersion)
        driver.execute(null, "PRAGMA user_version = $targetVersion;", 0)
    }
}
