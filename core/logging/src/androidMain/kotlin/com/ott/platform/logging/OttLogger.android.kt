package com.ott.platform.logging

internal actual fun logPlatform(level: String, tag: String, message: String, throwable: Throwable?) {
    println("$level/$tag: $message")
    throwable?.printStackTrace()
}
