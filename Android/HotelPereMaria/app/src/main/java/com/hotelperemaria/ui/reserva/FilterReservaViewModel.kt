package com.hotelperemaria.ui.reserva

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.data.model.Reserva
import com.hotelperemaria.utils.DateUtils.convertMillisToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject


// 更新 SortingOption 定义
enum class SortingOption(val field: String, val ascending: Boolean) {
    PRICE_ASC("price", true),
    PRICE_DESC("price", false),
    DATE_ASC("date", true),
    DATE_DESC("date", false),
    ROOM_TYPE_ASC("roomType", true),  // 注意这里的命名
    ROOM_TYPE_DESC("roomType", false),
    EXTRAS_ASC("extras", true),
    EXTRAS_DESC("extras", false);

    // 添加安全获取方法
    companion object {
        fun fromFieldAndDirection(field: String, ascending: Boolean): SortingOption? {
            return entries.find {
                it.field == field && it.ascending == ascending
            }
        }
    }
}

data class FilterReservaUiState(
    val startDate: String = "",
    val endDate: String = "",
    val selectedDateRange: Pair<Long?, Long?>? = null,
    val showDatePicker: Boolean = false,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..5000f,
    var selectedPriceRange: ClosedFloatingPointRange<Float> = 0f..5000f,
    val activeSorts: List<SortingOption> = emptyList(),
    val selectedServices: List<String> = emptyList(),
    val selectedRoomType: String? = null,
    val availableServices: List<String> = emptyList(),
    val availableRoomTypes: List<String> = emptyList(),
)


@HiltViewModel
class FilterReservaViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(FilterReservaUiState())
    val uiState: StateFlow<FilterReservaUiState> = _uiState.asStateFlow()

    // 模拟数据
    private val _mockReservas = mutableStateListOf(
        Reserva(
            id = "1",
            id_habitacion = "101",
            fecha_entrada = Instant.parse("2024-06-01T12:00:00Z"),
            fecha_salida = Instant.parse("2024-06-05T12:00:00Z"),
            huespedes = 2,
            precio_total = BigDecimal(1200),
            extrasSeleccionados = listOf(
                ExtraService("WiFi", BigDecimal(50)),
                ExtraService("Desayuno", BigDecimal(100))
            )
        ).apply {
            tipo_habitacion = "Deluxe"
        },
        // 添加更多模拟数据...
    )

    init {
        _uiState.value = _uiState.value.copy(
            availableServices = _mockReservas.flatMap { it.extrasSeleccionados.map { e -> e.nombre } }
                .distinct(),
            availableRoomTypes = _mockReservas.map { it.tipo_habitacion }.distinct()
        )
    }
    fun updateSorts(newSort: SortingOption) {
        /*_activeSorts.update { currentSorts ->
            currentSorts
                .filterNot { it.field == newSort.field }
                .plus(newSort)
        }*/
        _uiState.update {
            it.copy(activeSorts = it.activeSorts.filterNot { s -> s.field == newSort.field }.plus(newSort))
        }
    }

    fun removeSort(sort: SortingOption) {
        _uiState.update {
            it.copy(activeSorts = it.activeSorts.filterNot { s -> s == sort })
        }
    }

    fun removeSortsByField(field: String) {
        _uiState.update {
            it.copy(activeSorts = it.activeSorts.filterNot { s -> s.field == field })
        }
    }

    fun updateSelectedServices(services: List<String>) {
        _uiState.update { it.copy(selectedServices = services) }
    }

    fun clearSelectedServices() {
        _uiState.update { it.copy(selectedServices = emptyList()) }
    }


    // 筛选后的数据
    val filteredReservas by derivedStateOf {
        _mockReservas
            .filter { reserva ->
                // 日期筛选
                (uiState.value.selectedDateRange?.let { (start, end) ->
                    (start == null || reserva.fecha_entrada.toEpochMilli() >= start) &&
                            (end == null || reserva.fecha_salida.toEpochMilli() <= end)
                } ?: true) &&

                        // 服务筛选
                        (uiState.value.selectedServices.isEmpty() || uiState.value.selectedServices.all { service ->
                            reserva.extrasSeleccionados.any { it.nombre == service }
                        }) &&

                        // 房型筛选
                        (uiState.value.selectedRoomType == null || reserva.tipo_habitacion == uiState.value.selectedRoomType) &&

                        // 价格筛选
                        (reserva.precio_total.toFloat() in uiState.value.selectedPriceRange)
            }
            .sortedWith(compareByMultiple(uiState.value.activeSorts))
    }

    private fun compareByMultiple(sorts: List<SortingOption>): Comparator<Reserva> {
        return sorts.fold(Comparator { _, _ -> 0 }) { comparator, sort ->
            comparator.then(
                when (sort) {
                    SortingOption.PRICE_ASC -> compareBy { it.precio_total }
                    SortingOption.PRICE_DESC -> compareByDescending { it.precio_total }
                    SortingOption.DATE_ASC -> compareBy { it.fecha_entrada }
                    SortingOption.DATE_DESC -> compareByDescending { it.fecha_entrada }
                    SortingOption.ROOM_TYPE_ASC -> compareBy { it.tipo_habitacion }
                    SortingOption.ROOM_TYPE_DESC -> compareByDescending { it.tipo_habitacion }
                    SortingOption.EXTRAS_ASC -> compareBy { it.extrasSeleccionados.size }
                    SortingOption.EXTRAS_DESC -> compareByDescending { it.extrasSeleccionados.size }
                }
            )
        }
    }

    fun updateSelectedRoomType(roomType: String?) {
        _uiState.update { it.copy(selectedRoomType = roomType) }
    }

    fun updateSelectedPrice(priceRange: ClosedFloatingPointRange<Float>) {
        _uiState.update { it.copy(selectedPriceRange = priceRange) }
    }

    fun clearSlectedPrice() {
        _uiState.update { it.copy(selectedPriceRange = it.priceRange) }
    }

    fun clearDates() {
        _uiState.update { it.copy(startDate = "", endDate = "", selectedDateRange = null) }
        /*startDate.value = ""
        endDate.value = ""
        selectedDateRange.value = null*/
    }

    fun updateDateRange(startMillis: Long?, endMillis: Long?) {
       /* startDate.value = startMillis?.let { convertMillisToLocalDate(it) } ?: ""
        endDate.value = endMillis?.let { convertMillisToLocalDate(it) } ?: ""*/
        _uiState.update {uiState ->
            uiState.copy(
                startDate = startMillis?.let { convertMillisToLocalDate(it) } ?: "",
                endDate = endMillis?.let { convertMillisToLocalDate(it) } ?: "",
                selectedDateRange = Pair(startMillis, endMillis)
            )
        }
    }

    fun toggleDatePicker(show: Boolean) {
        _uiState.update { it.copy(showDatePicker = show) }
    }
}