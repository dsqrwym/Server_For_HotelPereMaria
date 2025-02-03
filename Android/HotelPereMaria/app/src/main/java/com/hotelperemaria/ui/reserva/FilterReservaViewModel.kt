package com.hotelperemaria.ui.reserva

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
        ).apply {
            tipo_habitacion = "Deluxe"
        },
        // 添加更多模拟数据...
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


}