package com.hotelperemaria.data.auth

import com.hotelperemaria.data.model.RefreshTokenRequest
import com.hotelperemaria.data.network.ApiAuthService
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.io.IOException
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiAuthService: ApiAuthService
) : Authenticator {
    private val refreshLock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 防止多次重试
        if (response.code != 401) return null
        if (responseCount(response) >= 2) return null
        val oldAccessToken = sessionManager.getAccessToken()
        val refreshToken = sessionManager.getRefreshToken() ?: run {
            sessionManager.clearTokens()
            return null
        }

        try {
            refreshLock.lock()
            // 再次检查 Token 是否已被其他线程刷新

            if (sessionManager.getAccessToken() != oldAccessToken) {
                return response.request // 直接重试
            }
            // 同步刷新 Token
            val call = apiAuthService.refreshToken(RefreshTokenRequest(refreshToken))
            val res = try {
                call.execute()
            } catch (e: IOException) {
                return null
            }

            return if (res.isSuccessful) {
                res.body()?.let {
                    sessionManager.refreshTokens(it.accessToken, refreshToken)
                    // 返回新请求
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${it.accessToken}")
                        .build()
                    return null
                }
            } else {
                sessionManager.clearTokens()
                null // 触发重新登录
            }
        } finally {
            refreshLock.unlock()
        }
    }
}

private fun responseCount(response: Response): Int {
    var count = 1
    var r = response
    while (r.priorResponse != null) {
        count++
        r = r.priorResponse!!
    }
    return count
}
