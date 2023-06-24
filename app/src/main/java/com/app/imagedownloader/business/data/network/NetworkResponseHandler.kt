package com.app.imagedownloader.business.domain.NetworkBoundResource

import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.business.domain.DataState.DataState
import com.app.imagedownloader.framework.Utils.Logger
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

abstract class NetworkResponseHandler<ViewStateType, ResponseObject>(
    val customStateOnInit: Boolean = false,
    customState: (() -> DataState<ViewStateType>?)? = null
) {

    private lateinit var apiResult: ApiResult<ResponseObject>

    var result: Flow<DataState<ViewStateType>>

    init {
        result = flow {

            if (customStateOnInit && customState != null) {
                emit(customState()!!)
            } else {
                emit(DataState.loading(true))
            }

            withContext(Dispatchers.Default) {
                apiResult = doNetworkCall()
            }
            when (apiResult) {
                is ApiResult.Success -> {
                    Logger.log("659595  abstracy........ = 445")
                    emit(createSuccessDataState(apiResult as ApiResult.Success<ResponseObject>))
                }
                is ApiResult.Error -> {
                    Logger.log("659595  abstracy........ =4 ")
                    emit(DataState.error((apiResult as ApiResult.Error<ResponseObject>).error))
                }
            }
        }
    }

    abstract suspend fun doNetworkCall(): ApiResult<ResponseObject>

    abstract fun createSuccessDataState(apiResult: ApiResult.Success<ResponseObject>): DataState<ViewStateType>

}