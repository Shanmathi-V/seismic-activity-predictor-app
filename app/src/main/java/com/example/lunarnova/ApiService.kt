package com.example.lunarnova

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict-seismic-events/")
    fun uploadFile(@Part file: MultipartBody.Part): Call<SeismicResponse>
}

//interface ApiService {
////    @Headers("Content-Type: application/json")
//    @POST("predict-seismic-events/")
//    fun uploadFile(@Body seismicRequest: SeismicRequest): Call<SeismicResponse>
//
//}