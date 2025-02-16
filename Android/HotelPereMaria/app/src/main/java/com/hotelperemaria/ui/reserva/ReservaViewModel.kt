package com.hotelperemaria.ui.reserva

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.data.model.Habitaciones
import com.hotelperemaria.data.model.ReservaForList
import com.hotelperemaria.data.repository.ReservationRepository
import com.hotelperemaria.data.repository.ReservationResult
import com.hotelperemaria.utils.DateUtils.convertDateToMillis
import com.hotelperemaria.utils.DateUtils.convertISO8601ToMillis
import com.hotelperemaria.utils.DateUtils.convertMillisToISO8601
import com.hotelperemaria.utils.DateUtils.convertMillisToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject


@HiltViewModel
class ReservaViewModel @Inject constructor(private val repository: ReservationRepository) :
    ViewModel() {
    //日期相关状态
    private val _startDate = MutableStateFlow("")
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow("")
    val endDate = _endDate.asStateFlow()

    private val _unselectableDates = MutableStateFlow<Set<Pair<Long?, Long?>>?>(null)
    val unselectableDates = _unselectableDates.asStateFlow()

    private val _huespedSelected = MutableStateFlow(1f)
    private val huespedSelected = _huespedSelected.asStateFlow()
    private val _maxHuesped = MutableStateFlow(10f)
    val maxHuesped = _maxHuesped.asStateFlow()


    //额外服务相关状态
    private val _extraServices = MutableStateFlow<List<ExtraService>>(emptyList())
    val extraServices = _extraServices.asStateFlow()

    private val _selectedServices = MutableStateFlow<Set<ExtraService>>(emptySet())
    val selectedServices = _selectedServices.asStateFlow()

    var nightlyPrice: BigDecimal? = BigDecimal.valueOf(50) // 后续可从 API 获取

    private val _selectedDays = MutableStateFlow(BigDecimal.ZERO)


    // 错误信息
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    // 计算总价格
    val totalPrice = combine(_selectedServices, _selectedDays) { services, days ->
        services.sumOf { it.precio } + (nightlyPrice?.multiply(days) ?: BigDecimal.ZERO)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    //加载动画
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var currentReservaId: String = ""
    private val _currentHabitacion = MutableStateFlow<Habitaciones?>(null)
    var currentHabitacion = _currentHabitacion.asStateFlow()

    fun creacionDeReserva(habitacion: Habitaciones) {
        _isLoading.value = true
        _currentHabitacion.value = habitacion
        Log.d("ReservaViewModel", "habitacionId: ${habitacion.id}")
        _extraServices.update {
            habitacion.extras.map { (k, v) ->
                ExtraService(
                    k,
                    BigDecimal.valueOf(v)
                )
            }
        }
        _maxHuesped.value = habitacion.capacidad.toFloat()
        nightlyPrice = BigDecimal.valueOf(habitacion.precio)
        _selectedServices.update { emptySet() }
        _selectedDays.value = BigDecimal.ONE
        _startDate.value = ""
        _endDate.value = ""
        _huespedSelected.value = 1f
        viewModelScope.launch {
            when (val result = repository.getReservedDate(habitacion.idMongo)) {
                is ReservationResult.Success -> {
                    _unselectableDates.value = result.data.map {
                        Pair(
                            convertISO8601ToMillis(it.fecha_entrada),
                            convertISO8601ToMillis(it.fecha_salida)
                        )
                    }.toSet()
                    _isLoading.value = false
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    // 处理额外服务选择
    fun toggleServiceSelection(service: ExtraService, isChecked: Boolean) {
        _selectedServices.update {
            if (isChecked) {
                it + service
            } else {
                it - service
            }
        }
    }

    // 更新选中的日期范围
    fun updateDateRange(startMillis: Long?, endMillis: Long?) {
        _startDate.value = startMillis?.let { convertMillisToLocalDate(it) } ?: ""
        _endDate.value = endMillis?.let { convertMillisToLocalDate(it) } ?: ""
        _selectedDays.value = if (startMillis != null && endMillis != null) {
            BigDecimal.valueOf(((endMillis - startMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(1))
        } else {
            BigDecimal.ZERO
        }
    }

    fun updateHuespedSelected(value: Float) {
        _huespedSelected.value = value
    }

    private fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (startDate.value.isEmpty() || endDate.value.isEmpty()) {
            errors.add("Por favor, introduce fecha valido")
        }

        if (huespedSelected.value < 1) {
            errors.add("Por favor, selecciona un numero valido de huespedes")
        }

        if (errors.isNotEmpty()) {
            _errorMessage.value = errors.joinToString("\n")
            return false
        }
        return true
    }

    fun confirmReservation() {
        if (validateForm()) {
            createReservation()
        }
    }

    fun modifyReservation() {
        if (validateForm()) {
            Log.d("ReservaViewModel", "currentDate: ${_startDate.value}")
            Log.d("ReservaViewModel", "currentDate: ${_endDate.value}")
            Log.d(
                "ReservaViewModel",
                "currentDate: ${convertDateToMillis(endDate.value)?.let { convertMillisToISO8601(it) }}"
            )

            _isLoading.value = true
            viewModelScope.launch {
                val result = repository.updateReserva(
                    currentReservaId,
                    currentHabitacion.value!!.idMongo,
                    huespedSelected.value.toInt(),
                    Instant.parse(
                        convertDateToMillis(startDate.value)?.let { convertMillisToISO8601(it) }
                            ?: startDate.value
                    ),
                    Instant.parse(
                        convertDateToMillis(endDate.value)?.let { convertMillisToISO8601(it) }
                            ?: endDate.value),
                    selectedServices.value.toList()
                )
                when (result) {
                    is ReservationResult.Success -> {
                        _isLoading.value = false
                    }

                    is ReservationResult.Error -> {
                        _isLoading.value = false
                        _errorMessage.value = result.message
                    }

                    else -> {
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun inicialModifyReservaScreen(reserva: ReservaForList) {
        _isLoading.value = true
        currentReservaId = reserva.id
        viewModelScope.launch {
            when (val result = repository.getReservaById(reserva.id)) {
                is ReservationResult.Success -> {
                    _extraServices.value = result.data.id_habitacion.extras
                    _maxHuesped.value = result.data.id_habitacion.Capacidad.toFloat()
                    _startDate.value =
                        result.data.fecha_entrada?.let { convertMillisToLocalDate(it.toEpochMilli()) }
                            .toString()
                    _endDate.value =
                        result.data.fecha_salida?.let { convertMillisToLocalDate(it.toEpochMilli()) }
                            .toString()
                    nightlyPrice = BigDecimal.valueOf(result.data.id_habitacion.precio_noche)
                    _huespedSelected.value = result.data.huespedes.toFloat()
                    _selectedServices.value = result.data.extrasSeleccionados?.toSet()
                        ?: reserva.extrasSeleccionados.toSet()
                    _selectedDays.value = BigDecimal.valueOf(
                        (
                                (result.data.fecha_salida?.toEpochMilli()
                                    ?: reserva.fecha_salida.toEpochMilli()) -
                                        (result.data.fecha_entrada?.toEpochMilli()
                                            ?: reserva.fecha_entrada.toEpochMilli())
                                ).toDouble() / (1000 * 60 * 60 * 24).toDouble()
                    ).coerceAtLeast(BigDecimal.ONE)
                    _currentHabitacion.value = Habitaciones(
                        result.data.id_habitacion._id,
                        result.data.id_habitacion._id,
                        result.data.id_habitacion.Tipo,
                        result.data.id_habitacion.Capacidad,
                        result.data.id_habitacion.precio_noche,
                        result.data.id_habitacion.extras.associate { it.nombre to it.precio.toDouble() },
                        result.data.id_habitacion.Images,
                        "",
                        ""
                    )
                    when (val date = repository.getReservedDate(
                        result.data.id_habitacion._id,
                        result.data._id
                    )) {
                        is ReservationResult.Success -> {
                            _unselectableDates.value = date.data.map {
                                Pair(
                                    convertISO8601ToMillis(it.fecha_entrada),
                                    convertISO8601ToMillis(it.fecha_salida)
                                )
                            }.toSet()
                            _isLoading.value = false
                        }

                        else -> {
                            _isLoading.value = false
                        }
                    }
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }

    private fun createReservation() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.createReserva(
                currentHabitacion.value!!.idMongo,
                huespedSelected.value.toInt(),
                Instant.parse(
                    convertDateToMillis(startDate.value)?.let { convertMillisToISO8601(it) }
                        ?: startDate.value
                ),
                Instant.parse(
                    convertDateToMillis(endDate.value)?.let { convertMillisToISO8601(it) }
                        ?: endDate.value),
                selectedServices.value.toList()
            )
            when (result) {
                is ReservationResult.Success -> {
                    _isLoading.value = false
                }

                is ReservationResult.Error -> {
                    _isLoading.value = false
                    _errorMessage.value = result.message
                }

                else -> {
                    _isLoading.value = false
                }
            }
        }
    }
}