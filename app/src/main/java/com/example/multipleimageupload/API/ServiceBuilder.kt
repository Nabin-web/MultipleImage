package com.example.multipleimageupload.API

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ServiceBuilder {
   private const val BASE_URL = "http://10.0.2.2:3000/"

//    var url:String? = BASE_URL + "images/"
    private val okHttp = OkHttpClient.Builder()

    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create()).client(
            okHttp.build()

        )

    private val retrofit = retrofitBuilder.build()

    //Generic class
    fun <T> buildService(ServiceType: Class<T>): T {
        return retrofit.create(ServiceType)
    }
}