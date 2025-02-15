package com.hotelperemaria.data.auth
import androidx.security.crypto.EncryptedSharedPreferences
import com.hotelperemaria.data.model.Usuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SessionManager @Inject constructor(private val encryptedPrefs: EncryptedSharedPreferences) {
    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_DATA = "user_data"
    }

    private val _currentUser = MutableStateFlow<Usuario?>(null)
    val currentUser = _currentUser.asStateFlow()

    private fun isLoggedIn(): Boolean {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null) != null
                && encryptedPrefs.getString(KEY_USER_DATA, null) != null
    }

    sealed class AuthEvent {
        data class LoginSuccess(val user: Usuario) : AuthEvent()
        data object Logout : AuthEvent()
        data class TokenExpired(val message: String) : AuthEvent()
    }
    // 使用 SharedFlow 处理一次性事件
    private val _authEvents = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val authEvents = _authEvents.asSharedFlow()

    // 使用 StateFlow 管理登录状态
    private val _loginState = MutableStateFlow(isLoggedIn())
    val loginState = _loginState.asStateFlow()

    init {
        encryptedPrefs.getString(KEY_USER_DATA, null)?.let { userData ->
            _currentUser.value = Json.decodeFromString(userData)
        }
    }

    fun saveSession(accessToken: String, refreshToken: String, user: Usuario) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, "Bearer $accessToken")
            .putString(KEY_REFRESH_TOKEN, "Bearer $refreshToken")
            .putString(KEY_USER_DATA, Json.encodeToString(user))
            .apply()
        _currentUser.value = user
        _loginState.value = true
        _authEvents.tryEmit(AuthEvent.LoginSuccess(user))
    }

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_DATA)
            .apply()
        _currentUser.value = null
        _loginState.value = false
        _authEvents.tryEmit(AuthEvent.Logout)
    }

    fun updateUserData(newUser: Usuario) {
        encryptedPrefs.edit()
            .putString(KEY_USER_DATA, Json.encodeToString(newUser))
            .apply()
        _currentUser.value = newUser
    }

    fun refreshTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, "Bearer $accessToken")
            .putString(KEY_REFRESH_TOKEN, "Bearer $refreshToken")
            .apply()
    }
}