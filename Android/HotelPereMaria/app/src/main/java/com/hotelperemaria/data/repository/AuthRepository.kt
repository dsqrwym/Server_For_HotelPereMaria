package com.hotelperemaria.data.repository

import android.util.Log
import com.hotelperemaria.data.auth.SessionManager
import com.hotelperemaria.data.model.LoginRequest
import com.hotelperemaria.data.network.ApiAuthService
import javax.inject.Inject


class AuthRepository @Inject constructor(
    private val apiAuthService: ApiAuthService,
    private val sessionManager: SessionManager
) {
    suspend fun login(
        username: String?,
        email: String?,
        password: String
    ): Result<Unit> {
        return try {
            val response = apiAuthService.login(LoginRequest(username, email, password))


            sessionManager.saveSession(
                response.accessToken, response.refreshToken, response.user
            )

            Log.d("Login", "${sessionManager.getAccessToken()}")
            Log.d("Login refresh", "${sessionManager.getRefreshToken()}")
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}