package com.hotelperemaria.ui.registro

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay

class registerViewModel : ViewModel() {

    private val _email= MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _pas= MutableLiveData<String>()
    val pas: LiveData<String> = _pas

    private val _regEnb= MutableLiveData<Boolean>()
    val regEnb: LiveData<Boolean> = _regEnb

    private val _isLoading= MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _Correcto = MutableLiveData<Boolean>()
    val correcto:LiveData<Boolean> =_Correcto

    fun onRegisChanged(email: String, pas: String){
        _email.value = email
        _pas.value = pas
        _regEnb.value = isValidEmail(email) && isValidPas(pas)

    }

    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun  isValidPas(pas :String):Boolean = pas.length >6

    suspend fun onRegisSelected() {
        _isLoading.value = true
        delay(4000)
        _isLoading.value =false
    }
}