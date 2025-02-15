package com.hotelperemaria.data.network

import com.hotelperemaria.data.model.Habitaciones
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiHabitacionService {
    @GET("/api/habitaciones/")
    suspend fun listHabs(@Header("Authorization") authToken: String): List<Habitaciones>
}