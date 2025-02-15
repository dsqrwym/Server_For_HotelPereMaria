package com.hotelperemaria.data.model

import kotlinx.serialization.Serializable

// 登录请求体
@Serializable
data class LoginRequest(val username: String?, val email: String?, val password: String)

// 登录响应
@Serializable
data class LoginResponse(val accessToken: String, val refreshToken: String, val user: Usuario)

// 刷新令牌请求体
@Serializable
data class RefreshTokenRequest(val refreshToken: String)

// 刷新令牌响应
@Serializable
data class RefreshTokenResponse(val accessToken: String)
