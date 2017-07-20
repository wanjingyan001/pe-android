package com.sogukj.pe.util

import com.framework.util.Errors
import com.google.gson.Gson

/**
 * Created by qff on 2016/1/26.
 */
class Result<T>() {
    var timestamp: Long = 0
    var code: Int = 0  // 200 is success other see errorCode list
    var message: String? = null
    var data: T? = null
    var exception: Throwable? = null


    fun ok(message: String): Result<*> {
        return Result(Errors.OK, message, null)
    }

    fun of(code: Int, message: String): Result<*> {
        return Result(code, message, null)
    }

    constructor(code: Int, message: String, data: T) : this() {
        this.code = code
        this.message = message
        this.data = data
    }


    val isOk: Boolean
        get() = code == 200 || code == Errors.OK

    companion object {
        private val TAG = Result::class.java.simpleName
        private val gson = Gson()

        fun of(code: Int, message: String, e: Throwable): Result<*> {
            val result = Result(code, message, null)
            result.exception = e
            return result
        }
    }
}
