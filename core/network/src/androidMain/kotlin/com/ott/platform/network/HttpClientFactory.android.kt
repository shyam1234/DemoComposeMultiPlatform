package com.ott.platform.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

actual fun createOttHttpClient(): HttpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) { json(OttJson.instance) }
    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 20_000
        socketTimeoutMillis = 60_000
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) = println("Ktor: $message")
        }
        level = LogLevel.INFO
    }
}
