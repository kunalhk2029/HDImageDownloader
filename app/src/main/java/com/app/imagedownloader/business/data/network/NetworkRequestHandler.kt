package com.app.imagedownloader.business.data.network.Volley.VolleyNetworkHandler

import com.android.volley.Request
import com.android.volley.Request.Method
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.app.imagedownloader.Utils.Constants.Constants
import com.app.imagedownloader.Utils.Constants.Constants.RetryPolicy
import com.app.imagedownloader.business.data.network.ApiResponses.InstagramApiResult.ApiResult
import com.app.imagedownloader.framework.Utils.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import org.json.JSONObject

abstract class NetworkRequestHandler<ResponseObject>(
    val url: String? = null,
    requestQueue: RequestQueue? = null,
    val map: HashMap<String, String>? = null,
    val fireConversionMethodOnError: Boolean = false,
    val scope: CoroutineScope = CoroutineScope(IO),
    val jsonObject: JSONObject?=null,
    val method: Int=Request.Method.GET,
    ) {

    private lateinit var apiResult: ApiResult<ResponseObject>

    var jsonObjConversionError = false
    private val job = Job()

    init {
        if (requestQueue != null) {
            Logger.log("659595  abstracy........ = 44fffff = "+url)
            val request = object : JsonObjectRequest(
                method, url,jsonObject,
                Response.Listener {
                    Logger.log("659595  abstracy........ = 44fffff  5 = "+it)
                    scope.launch {
                        val data = doJsonObjectConversion(it)
                        if (!jsonObjConversionError) {
                            apiResult = ApiResult.Success(data)
                        } else {
                            apiResult = ApiResult.Error(Constants.JSON_ERROR)
                        }
                        job.complete()
                    }
                },
                Response.ErrorListener {
                    Logger.log("659595  abstracy........ = 44fffff  5 =  5  55"+it)
                    Logger.log("5656298 yui = " + it.message)
                    scope.launch {
                        if (fireConversionMethodOnError) {
                            doJsonObjectConversion(null)
                        }
                    }
                    if (it.message?.contains("java.net.UnknownHostException") == true) {
                        apiResult = ApiResult.Error("No Internet")
                    } else {
                        apiResult = ApiResult.Error("Unknown Error")
                    }
                    job.complete()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    return map!!
                }
            }
            request.retryPolicy = RetryPolicy
            requestQueue.add(request)
        }
    }

    @Throws
    suspend fun getAsApiResult(): ApiResult<ResponseObject> {
        return withContext(Default) {
            while (!job.isCompleted) {
                delay(250L)
            }
            if (!::apiResult.isInitialized) {
                throw Exception("Something went wrong in Volley Network Handler Api Result Abstract class check it again..........")
            }
            apiResult
        }
    }

    abstract suspend fun doJsonObjectConversion(jsonObject: JSONObject?): ResponseObject
}

