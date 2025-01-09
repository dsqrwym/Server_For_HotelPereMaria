package com.hotelperemaria.ui.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

class loginViewModel: ViewModel(){

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
        _loginEnb.value = isValidEmail(email)

    }

    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()




    suspend fun onLoginSelected(){
        _isLoading.value = true
        delay(5000)
        _isLoading.value =false
        _Correcto.postValue(true)


    }




}