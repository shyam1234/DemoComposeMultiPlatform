package com.ott.platform.logging

interface OttLogger {
    fun d(tag: String, message: String)
    fun i(tag: String, message: String)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

object StdOutLogger : OttLogger {
    override fun d(tag: String, message: String) = println("D/$tag: $message")
    override fun i(tag: String, message: String) = println("I/$tag: $message")
    override fun w(tag: String, message: String, throwable: Throwable?) {
        println("W/$tag: $message")
        throwable?.printStackTrace()
    }
    override fun e(tag: String, message: String, throwable: Throwable?) {
        println("E/$tag: $message")
        throwable?.printStackTrace()
    }
}
