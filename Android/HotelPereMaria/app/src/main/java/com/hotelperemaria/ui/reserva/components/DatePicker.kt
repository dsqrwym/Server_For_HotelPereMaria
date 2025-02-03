package com.hotelperemaria.ui.reserva.components

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.ReservaViewModel
import java.util.Calendar

@Composable
fun DatePicker(viewModel: ReservaViewModel = hiltViewModel()) {
    // 使用 derivedStateOf 优化状态监听，只有关键状态变化时才重组
    val showDatePicker by remember { derivedStateOf { viewModel.showDatePicker.value } }

    // 使用 remember 缓存转换结果，避免每次重组都重新计算
    val unselectableDates = remember(viewModel.unselectableDates.value) {
        viewModel.unselectableDates.value?.mapNotNull { (first, second) ->
            if (first != null && second != null) Pair(first, second) else null
        }?.toSet() ?: emptySet()
    }

    Box(
        modifier = Modifier
            .padding(18.dp)
            .shadow(
                elevation = 10.dp,
                shape = MaterialTheme.shapes.medium
            )
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.medium
            )
    ) {
        // 使用 remember 缓存日期字符串，避免重复格式化
        val startDate = remember(viewModel.startDate.value) { viewModel.startDate.value }
        val endDate = remember(viewModel.endDate.value) { viewModel.endDate.value }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.showDatePicker.value = true }
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Fecha inicial
                Text(
                    text = stringResource(R.string.reservation_field_start_date),
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
                    text = stringResource(R.string.reservation_field_end_date),
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
            // 将不可选日期预处理移到 ViewModel，使用 remember 缓存
            DateRangePickerModal(
                onDateRangeSelected = { range ->
                    viewModel.updateDateRange(range.first, range.second)
                    viewModel.toggleDatePicker(false)
                },
                unselectableDates = unselectableDates,
            ){ viewModel.toggleDatePicker(false) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    unselectableDates: Set<Pair<Long, Long>>,
    noDateBeforeTaday: Boolean = true,
    dateInput: Boolean = false,
    onDismiss: () -> Unit
) {
    // 缓存日期区间预处理结果
    val dateRanges = remember(unselectableDates) {
        unselectableDates.map { it.first..it.second }.sortedBy { it.first }
    }

    val pickerState = androidx.compose.material3.rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                var isPast = false
                if (noDateBeforeTaday) {
                    isPast = utcTimeMillis < getStartOfToday()
                }
                val isInBlockedRange = dateRanges.any { range ->
                    utcTimeMillis in range
                }
                return !isPast && !isInBlockedRange
            }
        }
    )

    // 验证逻辑优化：使用派生状态减少计算次数
    val isInvalidSelection by remember(
        pickerState.selectedStartDateMillis,
        pickerState.selectedEndDateMillis
    ) {
        derivedStateOf {
            !isDateRangeValid(
                pickerState.selectedStartDateMillis,
                pickerState.selectedEndDateMillis,
                dateRanges
            )
        }
    }

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            pickerState.selectedStartDateMillis,
                            pickerState.selectedEndDateMillis
                        )
                    )
                },
                enabled = !isInvalidSelection
            ) {
                Text(stringResource(R.string.dialog_confirm))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    ) {
        androidx.compose.material3.DateRangePicker(
            state = pickerState,
            title = {
                Text(
                    text = if (!isInvalidSelection) stringResource(R.string.date_picker_dialog_title) else stringResource(
                        R.string.date_picker_dialog_title_error
                    ),
                    color = if (!isInvalidSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                )
            },
            showModeToggle = dateInput,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp)
        )
    }
}

// 独立验证函数，便于测试和复用
private fun isDateRangeValid(
    start: Long?,
    end: Long?,
    dateRanges: List<ClosedRange<Long>>
): Boolean {
    if (start == null || end == null) return true
    return dateRanges.none { range ->
        start <= range.endInclusive && end >= range.start
    }
}

fun getStartOfToday(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

@Preview
@Composable
fun DatePickerPreview() {
    DatePicker(viewModel = ReservaViewModel())
}