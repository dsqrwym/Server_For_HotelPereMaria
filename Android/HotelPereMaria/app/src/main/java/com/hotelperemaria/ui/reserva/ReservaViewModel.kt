package com.hotelperemaria.ui.reserva

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservaViewModel : ViewModel() {
    var startDate = mutableStateOf("")
    var endDate = mutableStateOf("")
    var showDatePicker = mutableStateOf(false)
    var unselectableDates = mutableStateOf<Set<Pair<Long, Long>>?>(null)
    var huespedSelected by mutableFloatStateOf(0f)
    var maxHuesped by mutableFloatStateOf(10f)

    // 初始化不可选择的日期范围
    init {
        unselectableDates.value = setOf(
            Pair(convertDateToMillis("02/02/2025"), convertDateToMillis("03/02/2025"))
        )
    }

    // 更新选中的日期范围
    fun updateDateRange(startMillis: Long?, endMillis: Long?) {
        startDate.value = startMillis?.let { convertMillisToDate(it) } ?: ""
        endDate.value = endMillis?.let { convertMillisToDate(it) } ?: ""
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