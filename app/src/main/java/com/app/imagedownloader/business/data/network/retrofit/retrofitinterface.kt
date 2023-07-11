package com.app.imagedownloader.business.data.network.retrofit

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface retrofitinterface {

    @GET
    @Streaming
    fun downloadMedia(@Url url: String): Call<ResponseBody>

    @FormUrlEncoded
    @POST
    suspend fun getPinterestMedia(
        @HeaderMap headermap: HashMap<String, String>,
        @Url url: String,
        @Field("source_url") source_url: String,
        @Field("data") data: String
    ): JsonObject
}