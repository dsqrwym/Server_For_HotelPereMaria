package com.hotelperemaria.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.hotelperemaria.data.model.ExtraService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReservaViewModel @Inject constructor() : ViewModel() {
    //日期相关状态
    var startDate = mutableStateOf("")
    var endDate = mutableStateOf("")
    var showDatePicker = mutableStateOf(false)
    var unselectableDates = mutableStateOf<Set<Pair<Long, Long>>?>(null)
    var huespedSelected by mutableFloatStateOf(0f)
    var maxHuesped by mutableFloatStateOf(10f)

    //额外服务相关状态
    var extraServices by mutableStateOf<List<ExtraService>>(emptyList())
    var selectedServices by mutableStateOf<Set<ExtraService>>(emptySet())
    val nightlyPrice: BigDecimal? = BigDecimal.valueOf(50) // 后续可从 API 获取
    private var selectedDays: BigDecimal? by mutableStateOf(BigDecimal.ZERO) // 根据日期选择计算

    // 初始化示例数据（后续替换为 API 调用)
    init {
        // 初始化不可选择的日期范围
        unselectableDates.value = setOf(
            Pair(convertDateToMillis("02/02/2025"), convertDateToMillis("03/02/2025"))
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

    // 计算总价格
    val totalPrice: BigDecimal
        get() = selectedServices.sumOf { it.precio } + (nightlyPrice!!.multiply(selectedDays))

    // 更新选中的日期范围
    fun updateDateRange(startMillis: Long?, endMillis: Long?) {
        startDate.value = startMillis?.let { convertMillisToDate(it) } ?: ""
        endDate.value = endMillis?.let { convertMillisToDate(it) } ?: ""
        selectedDays = if (startMillis != null && endMillis != null) {
            BigDecimal.valueOf((endMillis - startMillis) / (1000 * 60 * 60 * 24))
        } else {
            BigDecimal.ZERO
        }
    }

    // 切换日期选择器的显示状态
    fun toggleDatePicker(show: Boolean) {
        showDatePicker.value = show
    }

    // 验证选中的日期范围是否有效
    fun isDateRangeValid(startMillis: Long?, endMillis: Long?): Boolean {
        return startMillis == null || endMillis == null || unselectableDates.value?.none {
            startMillis < it.first && endMillis > it.second
        } ?: true
    }

    // 将毫秒转换为日期字符串
    private fun convertMillisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    // 将日期字符串转换为毫秒
    private fun convertDateToMillis(dateString: String): Long {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return try {
            val date = formatter.parse(dateString)
            date?.time ?: 0L
        } catch (e: ParseException) {
            0L
        }
    }
}