package com.example.lunarnova

import okhttp3.MultipartBody

data class SeismicRequest(
    val file: MultipartBody.Part
)
