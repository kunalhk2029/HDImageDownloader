package com.app.imagedownloader.business.data.network.retrofit

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    companion object {
        private val retrofit by lazy {

            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(logging).build()

            val gson = GsonBuilder()
                .setLenient()
                .create()
            Retrofit.Builder().baseUrl("https://i.instagram.com")
                .addConverterFactory(GsonConverterFactory.create(gson)).client(client).build()
        }

        val api by lazy {
            retrofit.create(retrofitinterface::class.java)
        }
    }
}