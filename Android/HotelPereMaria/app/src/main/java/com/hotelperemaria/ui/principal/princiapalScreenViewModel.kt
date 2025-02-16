package com.hotelperemaria.ui.principal

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotelperemaria.data.model.Habitaciones
import com.hotelperemaria.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrincipalScreenViewModel @Inject constructor(private val repository: RoomRepository) :
    ViewModel() {

    private val _habitacionesOriginal = MutableLiveData<List<Habitaciones>>()
    private val _habitaciones = MutableLiveData<List<Habitaciones>>()
    val habitaciones: LiveData<List<Habitaciones>> = _habitaciones

    private val _nombre = MutableLiveData<String>()
    val nombre: LiveData<String> = _nombre

    private val _tiposSeleccionados = MutableLiveData<List<String>>(emptyList())
    val tiposSeleccionados: LiveData<List<String>> = _tiposSeleccionados

    val tiposDisponibles = listOf("Individual", "Estandar", "Lujo", "Suite", "Duplex")

    init {
        obtenerHabitaciones()
    }

    private fun obtenerHabitaciones() {
        viewModelScope.launch {
            try {
                val response = repository.getHabList()
                _habitacionesOriginal.value = response
                _habitaciones.value = response
            } catch (e: Exception) {
                Log.e("API_Error", "Error al obtener habitaciones: ${e.message}")
            }
        }
    }

    fun buscador(nombre: String) {
        _nombre.value = nombre
        aplicarFiltros()
    }

    fun seleccionarTipo(tipo: String) {
        val listaActual = _tiposSeleccionados.value.orEmpty().toMutableList()
        if (listaActual.contains(tipo)) {
            listaActual.remove(tipo)
        } else {
            listaActual.add(tipo)
        }
        _tiposSeleccionados.value = listaActual
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val listaFiltrada = _habitacionesOriginal.value.orEmpty()
            .filter { it.nombre.contains(_nombre.value.orEmpty(), ignoreCase = true) }
            .filter { _tiposSeleccionados.value.isNullOrEmpty() || it.tipo in _tiposSeleccionados.value!! }

        _habitaciones.value = listaFiltrada
    }
}
