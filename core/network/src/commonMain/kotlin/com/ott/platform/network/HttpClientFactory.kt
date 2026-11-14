package com.ott.platform.network

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

object OttJson {
    val instance = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }
}

expect fun createOttHttpClient(): HttpClient
