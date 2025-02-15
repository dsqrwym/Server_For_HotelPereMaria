package com.hotelperemaria.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.data.model.Habitacion
import com.hotelperemaria.data.model.ReservaForList
import com.hotelperemaria.data.repository.ReservationRepository
import com.hotelperemaria.data.repository.ReservationResult
import com.hotelperemaria.utils.DateUtils.convertDateToMillis
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
    var maxHuesped by mutableFloatStateOf(10f)

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

    private fun creacionDeReserva(habitacion: Habitacion) {
        _extraServices.value = habitacion.extras
        maxHuesped = habitacion.Capacidad.toFloat()
        _selectedServices.value = emptySet()
        _selectedDays.value = BigDecimal.ZERO
        _startDate.value = ""
        _endDate.value = ""
        _huespedSelected.value = 1f
        nightlyPrice = BigDecimal.valueOf(habitacion.precio_noche)
        viewModelScope.launch {

        }
    }

    init {
        // 初始化不可选择的日期范围
        _unselectableDates.value = setOfNotNull(
            convertDateToMillis("10/02/2025")?.let {
                Pair(
                    it,
                    convertDateToMillis("16/02/2025") ?: it
                )
            },
            convertDateToMillis("20/02/2025")?.let {
                Pair(
                    it,
                    convertDateToMillis("26/02/2025") ?: it
                )
            }
        )
        // 初始化示例额外服务（后续替换为 API 数据）
        _extraServices.value = listOf(
            ExtraService("WI-FI", BigDecimal.valueOf(10)),
            ExtraService("Parking", BigDecimal.valueOf(20)),
            ExtraService("BreakFast", BigDecimal.valueOf(15)),
            ExtraService("GYM", BigDecimal.valueOf(18.5)),
            ExtraService("Swimming Pool", BigDecimal.valueOf(15)),
            ExtraService("Extra bed", BigDecimal.valueOf(15)),
        )

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

    }

    fun inicialModifyReservaScreen(reserva: ReservaForList) {
        viewModelScope.launch {
            val result = repository.getReservaById(reserva.id)
            when (result) {
                is ReservationResult.Success -> {
                    _extraServices.value = reserva.id_habitacion?.extras ?: emptyList()
                    maxHuesped = reserva.id_habitacion?.Capacidad?.toFloat() ?: 2.toFloat()
                    _startDate.value = convertMillisToLocalDate(reserva.fecha_entrada.toEpochMilli())
                    _endDate.value = convertMillisToLocalDate(reserva.fecha_salida.toEpochMilli())
                    _huespedSelected.value = reserva.huespedes.toFloat()
                    _selectedServices.value = reserva.extrasSeleccionados.toSet()
                    _selectedDays.value = BigDecimal.valueOf(
                        ((reserva.fecha_salida.toEpochMilli() - reserva.fecha_entrada.toEpochMilli()) / (1000 * 60 * 60 * 24)).coerceAtLeast(
                            1
                        )
                    )
                }
                else -> {}
            }
        }
    }

    private fun createReservation() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.createReserva("",
                huespedSelected.value.toInt(),
                Instant.parse(
                    convertDateToMillis(startDate.toString())?.let { convertMillisToISO8601(it) }
                ),
                Instant.parse(
                    convertDateToMillis(endDate.toString())?.let { convertMillisToISO8601(it) }),
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