package com.hotelperemaria.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.utils.DateUtils.convertDateToMillis
import com.hotelperemaria.utils.DateUtils.convertMillisToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ReservaViewModel @Inject constructor() : ViewModel() {

    //日期相关状态
    var startDate = mutableStateOf("")
    var endDate = mutableStateOf("")
    var showDatePicker = mutableStateOf(false)
    var unselectableDates = mutableStateOf<Set<Pair<Long?, Long?>>?>(null)
    var huespedSelected by mutableFloatStateOf(1f)
    var maxHuesped by mutableFloatStateOf(10f)

    //额外服务相关状态
    var extraServices by mutableStateOf<List<ExtraService>>(emptyList())
    var selectedServices by mutableStateOf<Set<ExtraService>>(emptySet())
    val nightlyPrice: BigDecimal? = BigDecimal.valueOf(50) // 后续可从 API 获取
    private var selectedDays: BigDecimal? by mutableStateOf(BigDecimal.ZERO) // 根据日期选择计算

    // 弹窗状态
    var showErrorDialog by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    // 计算总价格
    val totalPrice: BigDecimal
        get() = selectedServices.sumOf { it.precio } + (nightlyPrice!!.multiply(selectedDays))

    //加载动画
    var isLoading by mutableStateOf(false)
    // 初始化示例数据（后续替换为 API 调用)
    init {
        // 初始化不可选择的日期范围
        unselectableDates.value = setOfNotNull(
            convertDateToMillis("10/02/2025")?.let { Pair(it, convertDateToMillis("16/02/2025") ?: it) },
            convertDateToMillis("20/02/2025")?.let { Pair(it, convertDateToMillis("26/02/2025") ?: it) }
        )
        // 初始化示例额外服务（后续替换为 API 数据）
        extraServices = listOf(
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
        selectedServices = if (isChecked) {
            selectedServices + service
        } else {
            selectedServices - service
        }
    }

    // 更新选中的日期范围
    fun updateDateRange(startMillis: Long?, endMillis: Long?) {
        startDate.value = startMillis?.let { convertMillisToLocalDate(it) } ?: ""
        endDate.value = endMillis?.let { convertMillisToLocalDate(it) } ?: ""
        selectedDays = if (startMillis != null && endMillis != null) {
            BigDecimal.valueOf(((endMillis - startMillis) / (1000 * 60 * 60 * 24)).coerceAtLeast(1)) // 确保最小为 1 天
        } else {
            BigDecimal.ZERO
        }
    }

    // 切换日期选择器的显示状态
    fun toggleDatePicker(show: Boolean) {
        showDatePicker.value = show
    }

    private fun validateForm(): Boolean {
        val errors = mutableListOf<String>()

        if (startDate.value.isEmpty() || endDate.value.isEmpty()) {
            errors.add("Por favor, introduce fecha valido")
        }

        if (huespedSelected < 1) {
            errors.add("Por favor, selecciona un numero valido de huespedes")
        }

        if (errors.isNotEmpty()) {
            errorMessage = errors.joinToString("\n")
            showErrorDialog = true
            return false
        }
        return true
    }

    fun confirmReservation() {
        if (validateForm()) {
            createReservation()
        }
    }

    private fun createReservation() {
        isLoading = true
    }
}