package com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult

sealed class ApiResult<T> {
    data class Success<T>(val data:T):ApiResult<T>()
    data class Error<T>(val error:String?):ApiResult<T>()
}