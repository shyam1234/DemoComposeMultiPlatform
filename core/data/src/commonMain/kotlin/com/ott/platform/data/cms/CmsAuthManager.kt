package com.ott.platform.data.cms

import com.ott.platform.logging.OttLogger
import com.ott.platform.network.OttJson
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CmsAuthManager(
    private val httpClient: HttpClient,
    private val logger: OttLogger,
    private val config: CmsConfig = CmsConfig(),
) {
    private val mutex = Mutex()
    private var accessToken: String? = null
    private var refreshToken: String? = null

    suspend fun getAccessToken(forceRefresh: Boolean = false): String = mutex.withLock {
        if (!forceRefresh) {
            accessToken?.takeIf { it.isNotBlank() }?.let { return@withLock it }
        }
        loginInternal()
    }

    suspend fun invalidateToken() {
        mutex.withLock {
            accessToken = null
        }
    }

    private suspend fun loginInternal(): String {
        val url = config.loginUrl()
        logger.d(TAG, "Authenticating with CMS login endpoint")
        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                OttJson.instance.encodeToString(
                    CmsLoginRequestDto(
                        username = config.username,
                        password = config.password,
                    ),
                ),
            )
        }
        val rawBody = response.bodyAsText()
        if (!response.status.isSuccess()) {
            error("CMS login failed (${response.status.value}): ${rawBody.take(200)}")
        }
        val dto = OttJson.instance.decodeFromString<CmsLoginResponseDto>(rawBody)
        val token = dto.access.trim()
        if (token.isEmpty()) {
            error("CMS login returned an empty access token")
        }
        accessToken = token
        refreshToken = dto.refresh.takeIf { it.isNotBlank() }
        return token
    }

    private companion object {
        const val TAG = "CmsAuth"
    }
}
