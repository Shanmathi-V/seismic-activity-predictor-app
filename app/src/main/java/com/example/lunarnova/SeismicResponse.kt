package com.example.lunarnova

data class SeismicResponse(

    val predicted_seismic_events: List<Int>,
    val plot: String,
    val spectrogram: String
)