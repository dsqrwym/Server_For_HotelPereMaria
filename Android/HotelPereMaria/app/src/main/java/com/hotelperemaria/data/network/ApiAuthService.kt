package com.hotelperemaria.data.network

import com.hotelperemaria.data.model.ApiResponse
import com.hotelperemaria.data.model.LoginRequest
import com.hotelperemaria.data.model.LoginResponse
import com.hotelperemaria.data.model.RefreshTokenRequest
import com.hotelperemaria.data.model.RefreshTokenResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiAuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh-token")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}