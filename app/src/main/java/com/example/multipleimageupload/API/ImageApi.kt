package com.example.multipleimageupload.API

import com.example.multipleimageupload.response.ImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import java.io.File

interface ImageApi {
    @Multipart
    @POST("upload/multiimage")
    suspend fun uploadImage(
        @Part file:MutableList<MultipartBody.Part>
    ): Response<ImageResponse>
}