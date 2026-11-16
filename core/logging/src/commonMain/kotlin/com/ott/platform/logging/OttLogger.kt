package com.ott.platform.logging

internal expect fun logPlatform(level: String, tag: String, message: String, throwable: Throwable?)

interface OttLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

object StdOutLogger : OttLogger {
    override fun d(tag: String, message: String) = logPlatform("D", tag, message, null)
    override fun i(tag: String, message: String) = logPlatform("I", tag, message, null)
    override fun w(tag: String, message: String, throwable: Throwable?) =
        logPlatform("W", tag, message, throwable)

    override fun e(tag: String, message: String, throwable: Throwable?) =
        logPlatform("E", tag, message, throwable)
}
