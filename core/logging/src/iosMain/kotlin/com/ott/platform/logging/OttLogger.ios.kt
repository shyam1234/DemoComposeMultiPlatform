package com.ott.platform.logging

internal actual fun logPlatform(level: String, tag: String, message: String, throwable: Throwable?) {
    val full = buildString {
        append("$level/$tag: $message")
        if (throwable != null) {
            append("\n")
            append(throwable.stackTraceToString())
        }
    }
    println(full)
}
