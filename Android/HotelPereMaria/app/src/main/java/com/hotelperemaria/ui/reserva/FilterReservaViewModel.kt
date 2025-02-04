package com.hotelperemaria.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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


enum class SortingOption(val field: String, val ascending: Boolean) {
    PRICE_ASC("price", true),
    PRICE_DESC("price", false),
    DATE_ASC("date", true),
    DATE_DESC("date", false),
    ROOM_TYPE_ASC("roomType", true),
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

// 状态分层架构
sealed interface FilterState {
    // 基础状态接口
    val availableServices: List<String>
    val availableRoomTypes: List<String>
}

data class DateFilterState(
    val startDate: String = "",
    val endDate: String = "",
    val selectedDateRange: Pair<Long?, Long?>? = null,
    val showPicker: Boolean = false
) : FilterState {
    override val availableServices: List<String> = emptyList()
    override val availableRoomTypes: List<String> = emptyList()
}

data class PriceFilterState(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..5000f,
    var selectedPrice: ClosedFloatingPointRange<Float> = 0f..5000f
) : FilterState {
    override val availableServices: List<String> = emptyList()
    override val availableRoomTypes: List<String> = emptyList()
}

data class ServiceFilterState(
    val selectedServices: List<String> = emptyList()
) : FilterState {
    override val availableServices: List<String> = emptyList()
    override val availableRoomTypes: List<String> = emptyList()
}

data class RoomTypeFilterState(
    val selectedRoomType: String? = null
) : FilterState {
    override val availableServices: List<String> = emptyList()
    override val availableRoomTypes: List<String> = emptyList()
}

data class SortState(
    val activeSorts: List<SortingOption> = emptyList()
) : FilterState {
    override val availableServices: List<String> = emptyList()
    override val availableRoomTypes: List<String> = emptyList()
}

data class CompositeFilterState(
    override val availableServices: List<String>,
    override val availableRoomTypes: List<String>,
    val dateState: DateFilterState = DateFilterState(),
    val priceState: PriceFilterState = PriceFilterState(),
    val serviceState: ServiceFilterState = ServiceFilterState(),
    val roomTypeState: RoomTypeFilterState = RoomTypeFilterState(),
    val sortState: SortState = SortState()
) : FilterState


@HiltViewModel
class FilterReservaViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<FilterState>(
        CompositeFilterState(
            availableServices = emptyList(),
            availableRoomTypes = emptyList()
        )
    )
    val state: StateFlow<FilterState> = _state.asStateFlow()
    var isLoading by mutableStateOf(false)

    // 添加过滤排序后的结果状态
    private val _filteredReservas = MutableStateFlow<List<Reserva>>(emptyList())
    val filteredReservas: StateFlow<List<Reserva>> = _filteredReservas.asStateFlow()

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
        ).apply { tipo_habitacion = "Deluxe" },

        Reserva(
            id = "2",
            id_habitacion = "202",
            fecha_entrada = Instant.parse("2024-06-10T15:00:00Z"),
            fecha_salida = Instant.parse("2024-06-15T11:00:00Z"),
            huespedes = 1,
            precio_total = BigDecimal(800),
            extrasSeleccionados = listOf(
                ExtraService("Gimnasio", BigDecimal(30))
            )
        ).apply { tipo_habitacion = "Single" },

        Reserva(
            id = "3",
            id_habitacion = "303",
            fecha_entrada = Instant.parse("2024-07-01T14:00:00Z"),
            fecha_salida = Instant.parse("2024-07-07T12:00:00Z"),
            huespedes = 3,
            precio_total = BigDecimal(1800),
            extrasSeleccionados = listOf(
                ExtraService("WiFi", BigDecimal(50)),
                ExtraService("Desayuno", BigDecimal(100)),
                ExtraService("Spa", BigDecimal(200))
            )
        ).apply { tipo_habitacion = "Suite" },

        Reserva(
            id = "4",
            id_habitacion = "404",
            fecha_entrada = Instant.parse("2024-05-20T10:00:00Z"),
            fecha_salida = Instant.parse("2024-05-22T12:00:00Z"),
            huespedes = 2,
            precio_total = BigDecimal(500),
            extrasSeleccionados = emptyList()
        ).apply { tipo_habitacion = "Standard" },

        Reserva(
            id = "5",
            id_habitacion = "505",
            fecha_entrada = Instant.parse("2024-08-15T12:00:00Z"),
            fecha_salida = Instant.parse("2024-08-20T12:00:00Z"),
            huespedes = 4,
            precio_total = BigDecimal(2500),
            extrasSeleccionados = listOf(
                ExtraService("Desayuno", BigDecimal(100)),
                ExtraService("Piscina", BigDecimal(150))
            )
        ).apply { tipo_habitacion = "Family" },
        Reserva(
            id = "6",
            id_habitacion = "606",
            fecha_entrada = Instant.parse("2024-09-10T14:00:00Z"),
            fecha_salida = Instant.parse("2024-09-15T12:00:00Z"),
            huespedes = 1,
            precio_total = BigDecimal(700),
            extrasSeleccionados = listOf(
                ExtraService("WiFi", BigDecimal(50)),
                ExtraService("Desayuno", BigDecimal(100))
            )
        ).apply { tipo_habitacion = "Business" },

        Reserva(
            id = "7",
            id_habitacion = "707",
            fecha_entrada = Instant.parse("2024-10-01T13:00:00Z"),
            fecha_salida = Instant.parse("2024-10-05T11:00:00Z"),
            huespedes = 2,
            precio_total = BigDecimal(1100),
            extrasSeleccionados = listOf(
                ExtraService("Spa", BigDecimal(200)),
                ExtraService("Cena romántica", BigDecimal(150))
            )
        ).apply { tipo_habitacion = "Honeymoon Suite" },

        Reserva(
            id = "8",
            id_habitacion = "808",
            fecha_entrada = Instant.parse("2024-11-15T12:00:00Z"),
            fecha_salida = Instant.parse("2024-11-20T12:00:00Z"),
            huespedes = 5,
            precio_total = BigDecimal(3000),
            extrasSeleccionados = listOf(
                ExtraService("Transporte al aeropuerto", BigDecimal(80)),
                ExtraService("Desayuno", BigDecimal(150)),
                ExtraService("Tour por la ciudad", BigDecimal(300))
            )
        ).apply { tipo_habitacion = "Penthouse" },

        Reserva(
            id = "9",
            id_habitacion = "909",
            fecha_entrada = Instant.parse("2024-12-24T16:00:00Z"),
            fecha_salida = Instant.parse("2024-12-26T10:00:00Z"),
            huespedes = 3,
            precio_total = BigDecimal(1200),
            extrasSeleccionados = listOf(
                ExtraService("Cena de Navidad", BigDecimal(200)),
                ExtraService("WiFi", BigDecimal(50))
            )
        ).apply { tipo_habitacion = "Suite Navideña" },

        Reserva(
            id = "10",
            id_habitacion = "1010",
            fecha_entrada = Instant.parse("2025-01-05T15:00:00Z"),
            fecha_salida = Instant.parse("2025-01-20T12:00:00Z"),
            huespedes = 1,
            precio_total = BigDecimal(5000),
            extrasSeleccionados = listOf(
                ExtraService("Acceso VIP a spa", BigDecimal(500)),
                ExtraService("Servicio de mayordomo", BigDecimal(700)),
                ExtraService("Cata de vinos", BigDecimal(300))
            )
        ).apply { tipo_habitacion = "Ultra Luxury" },

        Reserva(
            id = "11",
            id_habitacion = "1111",
            fecha_entrada = Instant.parse("2025-02-10T12:00:00Z"),
            fecha_salida = Instant.parse("2025-02-14T12:00:00Z"),
            huespedes = 2,
            precio_total = BigDecimal(2000),
            extrasSeleccionados = listOf(
                ExtraService("Cena en la playa", BigDecimal(250)),
                ExtraService("Paseo en barco", BigDecimal(400))
            )
        ).apply { tipo_habitacion = "Romantic Getaway" },

        Reserva(
            id = "12",
            id_habitacion = "1212",
            fecha_entrada = Instant.parse("2025-03-05T14:00:00Z"),
            fecha_salida = Instant.parse("2025-03-10T11:00:00Z"),
            huespedes = 6,
            precio_total = BigDecimal(3500),
            extrasSeleccionados = listOf(
                ExtraService("Clase de cocina local", BigDecimal(200)),
                ExtraService("WiFi", BigDecimal(50)),
                ExtraService("Transporte al aeropuerto", BigDecimal(80))
            )
        ).apply { tipo_habitacion = "Villa Familiar" }
    )

    private fun loadInitialData() {
        _state.update {
            (it as CompositeFilterState).copy(
                availableServices = _mockReservas.flatMap { r ->
                    r.extrasSeleccionados.map { e -> e.nombre }
                }.distinct(),
                availableRoomTypes = _mockReservas.map { data -> data.tipo_habitacion }
                    .distinct()
            )
        }
    }

    init {
        loadInitialData()
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _state.value as CompositeFilterState
        val allReservas = _mockReservas.toList()

        // 应用日期过滤
        val dateFiltered = applyDateFilter(allReservas, currentState.dateState)

        // 应用价格过滤
        val priceFiltered = applyPriceFilter(dateFiltered, currentState.priceState)

        // 应用服务过滤
        val serviceFiltered = applyServiceFilter(priceFiltered, currentState.serviceState)

        // 应用房间类型过滤
        val roomTypeFiltered = applyRoomTypeFilter(serviceFiltered, currentState.roomTypeState)

        // 应用排序
        val sortedReservas = applySorting(roomTypeFiltered, currentState.sortState)

        _filteredReservas.value = sortedReservas
    }

    // 各个过滤条件的具体实现
    private fun applyDateFilter(
        reservas: List<Reserva>,
        dateState: DateFilterState
    ): List<Reserva> {
        val (startMillis, endMillis) = dateState.selectedDateRange ?: return reservas

        return reservas.filter { reserva ->
            val startDate = startMillis?.let { Instant.ofEpochMilli(it) }
            val endDate = endMillis?.let { Instant.ofEpochMilli(it) }

            // 检查预定时间段是否与筛选范围有重叠
            reserva.fecha_entrada < endDate && reserva.fecha_salida > startDate
        }
    }

    private fun applyPriceFilter(
        reservas: List<Reserva>,
        priceState: PriceFilterState
    ): List<Reserva> {
        return reservas.filter { reserva ->
            reserva.precio_total in BigDecimal.valueOf(priceState.selectedPrice.start.toDouble())..BigDecimal.valueOf(
                priceState.selectedPrice.endInclusive.toDouble()
            )
        }
    }

    private fun applyServiceFilter(
        reservas: List<Reserva>,
        serviceState: ServiceFilterState
    ): List<Reserva> {
        val selectedServices = serviceState.selectedServices
        if (selectedServices.isEmpty()) return reservas

        return reservas.filter { reserva ->
            selectedServices.all { serviceName ->
                reserva.extrasSeleccionados.any { it.nombre == serviceName }
            }
        }
    }

    private fun applyRoomTypeFilter(
        reservas: List<Reserva>,
        roomTypeState: RoomTypeFilterState
    ): List<Reserva> {
        return roomTypeState.selectedRoomType?.let { selectedType ->
            reservas.filter { it.tipo_habitacion == selectedType }
        } ?: reservas
    }

    // 排序实现
    private fun applySorting(reservas: List<Reserva>, sortState: SortState): List<Reserva> {
        val sorts = sortState.activeSorts
        if (sorts.isEmpty()) return reservas

        return reservas.sortedWith(compareByMultiple(sorts))
    }

    private fun updateDateState(updater: (DateFilterState) -> DateFilterState) {
        _state.update { current ->
            (current as CompositeFilterState).copy(
                dateState = updater(current.dateState)
            )
        }
        applyFilters()
    }

    private fun updatePriceState(updater: (PriceFilterState) -> PriceFilterState) {
        _state.update { current ->
            (current as CompositeFilterState).copy(
                priceState = updater(current.priceState)
            )
        }
        applyFilters()
    }

    private fun updateServiceState(updater: (ServiceFilterState) -> ServiceFilterState) {
        _state.update { current ->
            (current as CompositeFilterState).copy(
                serviceState = updater(current.serviceState)
            )
        }
        applyFilters()
    }

    private fun updateRoomTypeState(updater: (RoomTypeFilterState) -> RoomTypeFilterState) {
        _state.update { current ->
            (current as CompositeFilterState).copy(
                roomTypeState = updater(current.roomTypeState)
            )
        }
        applyFilters()
    }

    private fun updateSortState(updater: (SortState) -> SortState) {
        _state.update { current ->
            (current as CompositeFilterState).copy(
                sortState = updater(current.sortState)
            )
        }
        applyFilters()
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

    fun toggleDatePicker(show: Boolean) {
        updateDateState { it.copy(showPicker = show) }
    }

    fun updateDateRange(startMillis: Long?, endMillis: Long?) {
        /* startDate.value = startMillis?.let { convertMillisToLocalDate(it) } ?: ""
         endDate.value = endMillis?.let { convertMillisToLocalDate(it) } ?: ""*/
        updateDateState { dateState ->
            dateState.copy(
                startDate = startMillis?.let { convertMillisToLocalDate(it) } ?: "",
                endDate = endMillis?.let { convertMillisToLocalDate(it) } ?: "",
                selectedDateRange = Pair(startMillis, endMillis)
            )
        }
    }

    fun clearDates() {
        updateDateState { it.copy(startDate = "", endDate = "", selectedDateRange = null) }
        /*startDate.value = ""
        endDate.value = ""
        selectedDateRange.value = null*/
    }

    fun updateSorts(newSort: SortingOption) {
        /*_activeSorts.update { currentSorts ->
            currentSorts
                .filterNot { it.field == newSort.field }
                .plus(newSort)
        }*/
        updateSortState {
            it.copy(activeSorts = it.activeSorts.filterNot { s -> s.field == newSort.field }
                .plus(newSort))
        }
    }

    fun removeSort(sort: SortingOption) {
        updateSortState { it.copy(activeSorts = it.activeSorts.filterNot { s -> s == sort }) }
    }

    fun removeSortsByField(field: String) {
        updateSortState { it.copy(activeSorts = it.activeSorts.filterNot { s -> s.field == field }) }
    }


    fun updateSelectedServices(services: List<String>) {
        updateServiceState { it.copy(selectedServices = services) }
    }

    fun clearSelectedServices() {
        updateServiceState { it.copy(selectedServices = emptyList()) }
    }


    fun updateSelectedRoomType(roomType: String?) {
        updateRoomTypeState { it.copy(selectedRoomType = roomType) }
    }


    fun updateSelectedPrice(priceRange: ClosedFloatingPointRange<Float>) {
        updatePriceState { it.copy(selectedPrice = priceRange) }
    }

    fun clearSlectedPrice() {
        updatePriceState { it.copy(selectedPrice = it.priceRange) }
    }


    fun cancelReserva(reserva: Reserva) {
        _mockReservas.remove(reserva)
    }
}