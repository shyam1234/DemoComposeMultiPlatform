package com.ott.platform.database

import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.module.Module

internal const val OTT_DATABASE_NAME = "ott_platform.db"

internal expect val databaseIoDispatcher: CoroutineDispatcher

internal expect fun createDatabaseModule(): Module

val coreDatabaseModule: Module
    get() = createDatabaseModule()
