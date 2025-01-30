package com.hotelperemaria.ui.reserva.utils

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hotelperemaria.ui.reserva.ReservaViewModel

@Composable
fun DatePicker(viewModel: ReservaViewModel) {
    val startDate by remember { viewModel.startDate }
    val endDate by remember { viewModel.endDate }
    val showDatePicker by remember { viewModel.showDatePicker }
    val unselectableDates = remember { viewModel.unselectableDates }

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
                    viewModel.updateDateRange(range.first, range.second)
                    viewModel.toggleDatePicker(false)
                },
                unselectableDates = unselectableDates.value
            ) {
                viewModel.toggleDatePicker(false)
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
    val reservaViewModel = ReservaViewModel()
    var isInvalidSelection by remember { mutableStateOf(true) }
    // 从数据库获取不可选择的日期列表，若返回 null 则设置为空列表
    val dateRangePickerState = androidx.compose.material3.rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return unselectableDates?.none { utcTimeMillis < System.currentTimeMillis() || utcTimeMillis in it.first..it.second }
                    ?: true
            }
        }
    )

    isInvalidSelection = !reservaViewModel.isDateRangeValid(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis
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
                },
                enabled = !isInvalidSelection
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
                    text = if (!isInvalidSelection) "Seleccione rango de fechas" else "Rango de fechas inválido",
                    color = if (!isInvalidSelection) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
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

@Preview
@Composable
fun DatePickerPreview() {
    DatePicker(viewModel = ReservaViewModel())
}