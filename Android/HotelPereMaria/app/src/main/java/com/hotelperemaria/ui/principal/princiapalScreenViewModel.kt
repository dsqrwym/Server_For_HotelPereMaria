package com.hotelperemaria.ui.principal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotelperemaria.data.model.Habitaciones
import com.hotelperemaria.network.RetrofitServiceFactory
import kotlinx.coroutines.launch

class princiapalScreenViewModel:ViewModel() {

    private val _habitaciones = MutableLiveData<List<Habitaciones>>()
    val habitaciones: LiveData<List<Habitaciones>> = _habitaciones

    private val apiService = RetrofitServiceFactory.makeRetrofitService()

    init {
        obtenerHabitaciones()
    }

    fun obtenerHabitaciones() {
        viewModelScope.launch {
            try {
                val response = apiService.listHabs()
                // Verifica si la respuesta es válida
                if (response.isNotEmpty()) {
                    _habitaciones.value = response
                } else {
                    // Si la respuesta está vacía, puedes manejarlo aquí.
                    Log.d("API_Response", "Habitaciones obtenidas: $response")
                }
            } catch (e: Exception) {
                // Maneja cualquier error que ocurra durante la solicitud
                Log.e("API_Error", "Error al obtener las habitaciones: ${e.message}")
            }
        }
    }
}