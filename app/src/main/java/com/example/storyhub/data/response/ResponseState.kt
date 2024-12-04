package com.example.storyhub.data.response

sealed class ResponseState<out T> {
    data class Success<out R>(val data: R) : ResponseState<R>()
    data class Error(val exception: Throwable) : ResponseState<Nothing>()
    object Loading : ResponseState<Nothing>()
}
