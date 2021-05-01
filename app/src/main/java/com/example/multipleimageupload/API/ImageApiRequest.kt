package com.example.multipleimageupload.API

import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

abstract class ImageApiRequest {
    suspend fun <T : Any> apiRequest(call: suspend () -> Response<T>): T {
        val response = call.invoke()
        print(response)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            //   val error: String = response.errorBody().toString()
            val error = response.errorBody()?.string()
            val message = StringBuilder()
            error?.let {
                try {
                    message.append(JSONObject(it).getString("success"))
                } catch (e: JSONException) {
                }
                message.append("\n")
            }
            message.append("Error code : ${response.code()}")
            println(error)
            throw IOException(message.toString())
        }
    }
}