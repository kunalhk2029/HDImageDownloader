package com.app.imagedownloader.business.domain.core.DataState

import com.app.imagedownloader.business.domain.core.Event

data class DataState<T>(
    val data: Event<T>? = null,
    val message: Event<String>? = null,
    val loading: Boolean = false
) {
    companion object {
        fun <T> loading(loading: Boolean = true, data: T? = null): DataState<T> =
            DataState(Event.dataEvent(data), null, loading)

        fun <T> error(message: String?, data: T? = null): DataState<T> =
            DataState(Event.dataEvent(data), Event.messageEvent(message), false)

        fun <T> success(data: T? = null, loading: Boolean = false): DataState<T> =
            DataState(Event.dataEvent(data), null, loading)
    }
}
