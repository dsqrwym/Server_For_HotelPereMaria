package com.hotelperemaria.network

import com.hotelperemaria.data.model.Habitaciones
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface RetrofitService {
    @GET("/api/habitaciones/")
    suspend fun listHabs(): List<Habitaciones>
}

object RetrofitServiceFactory {
    private const val BASE_URL = "http://10.0.2.2:3000"
    private var authToken= "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3OTJhY2M4NDcxYzI5MDk3NDgxMWQ2ZiIsInRpcG8iOjUsImlhdCI6MTczOTUzNDk2MSwiZXhwIjoxNzM5NTM2NzYxfQ.7-a5_hEiydVT0Iyx3GBbcPFAi5we_lnkFeBJ0r-9jns"

    // Método para establecer el token
    fun setAuthToken(token: String) {
        authToken = token
    }

    private fun getHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request: Request = chain.request().newBuilder().apply {
                    authToken?.let {
                        addHeader("Authorization", "Bearer $authToken")
                    }
                }.build()
                chain.proceed(request)
            }
            .build()
    }

    fun makeRetrofitService(): RetrofitService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient()) // Usamos el cliente con el interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }
}