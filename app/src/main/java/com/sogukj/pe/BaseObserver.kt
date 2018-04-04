package com.sogukj.pe

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by admin on 2018/4/4.
 */
open class BaseObserver<T> : Observer<T> {
    override fun onComplete() {
    }

    override fun onNext(t: T) {
    }

    override fun onError(e: Throwable) {
    }

    override fun onSubscribe(d: Disposable) {
    }
}