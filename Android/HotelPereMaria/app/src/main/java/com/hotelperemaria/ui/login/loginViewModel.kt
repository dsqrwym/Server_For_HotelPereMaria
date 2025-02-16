package com.hotelperemaria.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotelperemaria.data.auth.SessionManager
import com.hotelperemaria.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
@HiltViewModel
class loginViewModel @Inject constructor(private val authRepository: AuthRepository, sessionManager: SessionManager) : ViewModel(){

    val sessionState: StateFlow<Boolean> = sessionManager.loginState
    // error
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // navegacion
    private val _navigateToHome = MutableLiveData<Boolean>()
    val navigateToHome: LiveData<Boolean> = _navigateToHome


    private val _email= MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _pas= MutableLiveData<String>()
    val pas: LiveData<String> = _pas

    private val _loginEnb= MutableLiveData<Boolean>()
    val loginEnb: LiveData<Boolean> = _loginEnb

    private val _isLoading= MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _Correcto = MutableLiveData<Boolean>()
    val correcto: LiveData<Boolean> =_Correcto


    fun onLoginChanged(email: String, pas: String) {
        _email.value = email
        _pas.value = pas
       // _loginEnb.value = isValidEmail(email)
        _loginEnb.value = true
    }

    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun clearMessage(){
        _errorMessage.value = null
    }

    suspend fun onLoginSelected(){
        _isLoading.value = true
        _errorMessage.value = null

        val email = _email.value ?: ""
        val password = _pas.value ?: ""

        viewModelScope.launch {
            try {
                val result = authRepository.login(
                    email = email,
                    password = password,
                    username = null
                )

                if (result.isSuccess) {
                    _navigateToHome.value = true
                    _Correcto.value = true
                } else {
                    result.onFailure {
                        _errorMessage.value = when (it) {
                            is IOException -> "Error de conexión a Internet. Verifica tu red e intenta nuevamente."
                            is HttpException -> "Error del servidor. Inténtalo de nuevo más tarde."
                            is NullPointerException,
                            is IllegalArgumentException,
                            is IllegalStateException,
                            is IndexOutOfBoundsException,
                            is UnsupportedOperationException,
                            is ArithmeticException,
                            is ClassCastException,
                            is AssertionError,
                            is NoSuchElementException,
                            is ConcurrentModificationException,
                            is Error,
                            is Exception-> "Se ha producido un error inesperado. Inténtalo de nuevo."
                            else -> "Cuenta o contraseña incorrecta. Verifica tus credenciales e inténtalo nuevamente."
                        }
                    }

                }
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is IOException -> "error internet"
                    else -> "undefined: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
                // 重置导航状态
                _navigateToHome.value = false
            }
        }
    }




}