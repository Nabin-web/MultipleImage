package com.example.multipleimageupload.repository

import com.example.multipleimageupload.API.ImageApi
import com.example.multipleimageupload.API.ImageApiRequest
import com.example.multipleimageupload.API.ServiceBuilder
import com.example.multipleimageupload.response.ImageResponse
import okhttp3.MultipartBody
import java.io.File

class imageRepository : ImageApiRequest() {

    val myapi = ServiceBuilder.buildService(ImageApi::class.java)

    suspend fun uploadImage(body: MutableList<MultipartBody.Part>): ImageResponse {
        return apiRequest {
            myapi.uploadImage(body)
        }
    }
}