package com.ott.platform.data.cms

data class CmsConfig(
    val baseUrl: String = DEFAULT_BASE_URL,
    val loginDeviceType: String = DEFAULT_LOGIN_DEVICE_TYPE,
    val menuDeviceType: String = DEFAULT_MENU_DEVICE_TYPE,
    val langCode: String = DEFAULT_LANG_CODE,
    val username: String = DEFAULT_USERNAME,
    val password: String = DEFAULT_PASSWORD,
    val fallbackLayoutId: String = DEFAULT_FALLBACK_LAYOUT_ID,
) {
    fun loginUrl(): String =
        "$baseUrl/v1/login?devicetype=$loginDeviceType"

    fun menuUrl(
        deviceType: String = menuDeviceType,
        languageCode: String = langCode,
    ): String = "$baseUrl/v1/menu?devicetype=$deviceType&lang_code=$languageCode"

    fun layoutUrl(layoutId: String): String =
        "$baseUrl/v1/layouts/$layoutId"

    fun railUrl(railId: String): String =
        "$baseUrl/v2/rails?id=$railId"

    companion object {
        const val DEFAULT_BASE_URL = "https://cms.play-cms.com"
        const val DEFAULT_LOGIN_DEVICE_TYPE = "atv_ott"
        const val DEFAULT_MENU_DEVICE_TYPE = "androidTV"
        const val DEFAULT_LANG_CODE = "en"
        const val DEFAULT_USERNAME = "dGF0YQ=="
        const val DEFAULT_PASSWORD = "U2FsdGVkX1974CQPT3eKjNplPPsojssg7HDC99WkzZU="
        const val DEFAULT_FALLBACK_LAYOUT_ID = "6af4302c6368a4cce6863ca6"
    }
}
