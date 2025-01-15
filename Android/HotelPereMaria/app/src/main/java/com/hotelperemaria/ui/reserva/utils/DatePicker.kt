package com.hotelperemaria.ui.reserva.utils

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DatePicker() {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(getDeviceDateFormat(context)) }
    var endDate by remember { mutableStateOf(getDeviceDateFormat(context)) }
    val unselectableDates = setOf(
        Pair(convertDateToMillis(context,"01/01/2025"), convertDateToMillis(context,"02/02/2025"))
    )

    Box(
        modifier = Modifier
            .padding(18.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.medium
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Fecha inicial
                Text(
                    text = "Fecha Inicial",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = startDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column {
                // Fecha final
                Text(
                    text = "Fecha Final",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = endDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 弹出 DateRangePicker 对话框
        if (showDatePicker) {

                DateRangePickerModal(
                    onDateRangeSelected = { range ->
                        startDate = range.first?.let { convertMillisToDate(context, it) }.toString()
                        endDate = range.second?.let { convertMillisToDate(context, it) }.toString()
                        showDatePicker = false
                    },
                    unselectableDates = unselectableDates
                ) {
                    showDatePicker = false
                }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    unselectableDates: Set<Pair<Long, Long>>? = null,
    onDismiss: () -> Unit
) {
    // 从数据库获取不可选择的日期列表，若返回 null 则设置为空列表
    val dateRangePickerState = androidx.compose.material3.rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
               return unselectableDates?.none {utcTimeMillis in it.first..it.second} ?: true
            }
        }
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        androidx.compose.material3.DateRangePicker(
            state = dateRangePickerState,
            title = {
                Text(
                    text = "Seleccione rango de fechas"
                )
            },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}

fun getDeviceDateFormat(context: Context): String {
    return try {
        // 使用Context获取设备的日期格式
        val dateFormat = DateFormat.getDateFormat(context)
        (dateFormat as SimpleDateFormat).toPattern()
    } catch (e: Exception) {
        // 处理异常并返回默认格式
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).toPattern()
    }
}

// 将毫秒转换为日期字符串
fun convertMillisToDate(context: Context, millis: Long): String {
    val formatter = SimpleDateFormat(getDeviceDateFormat(context), Locale.getDefault())
    return formatter.format(Date(millis))
}

// 将日期字符串转换为毫秒
fun convertDateToMillis(context: Context, dateString: String): Long {
    val formatter = SimpleDateFormat(getDeviceDateFormat(context), Locale.getDefault())
    return try {
        val date = formatter.parse(dateString)
        date?.time ?: 0L
    } catch (e: ParseException) {
        0L
    }
}

@Preview
@Composable
fun DatePickerPreview() {
    DatePicker()
}