package com.sogukj.pe.service

import com.google.gson.Gson
import com.sogukj.pe.util.Errors

/**
 * Created by qff on 2016/1/26.
 */
class Payload<T>() {
    var timestamp: Long = System.currentTimeMillis()
    var code: Int = 0  // 200 is success other see errorCode list
    var message: String? = null
    var payload: T? = null
    var total:Any? = null
    var exception: Throwable? = null


    fun ok(message: String): Payload<*> {
        return Payload(Errors.OK, message, null)
    }

    fun of(code: Int, message: String): Payload<*> {
        return Payload(code, message, null)
    }

    constructor(code: Int, message: String, data: T) : this() {
        this.code = code
        this.message = message
        this.payload = data
    }


    val isOk: Boolean
        get() = if (code == 200 || code == Errors.OK)
            true
        else if (message != null && message == "ok")
            true
        else false

    companion object {
        private val TAG = Payload::class.java.simpleName
        private val gson = Gson()

        fun of(code: Int, message: String, e: Throwable): Payload<*> {
            val result = Payload(code, message, null)
            result.exception = e
            return result
        }
    }
}
