package com.app.imagedownloader.business.data.network.retrofit

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

interface retrofitinterface {

    @GET
    @Streaming
    fun downloadMedia(@Url url: String): Call<ResponseBody>

}