package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.CompositeFilterState
import com.hotelperemaria.ui.reserva.FilterReservaViewModel
import com.hotelperemaria.ui.reserva.components.DateRangePickerModal


@Composable
fun FilterBar(
    modifier: Modifier = Modifier,
    viewModel: FilterReservaViewModel
) {
    val state by viewModel.state.collectAsState()
    val compositeState = state as? CompositeFilterState ?: return

    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                ),
            horizontalAlignment = Alignment.End
        ) {
            if (compositeState.dateState.showPicker) {
                DateRangePickerModal(
                    onDateRangeSelected = { range ->
                        viewModel.updateDateRange(range.first, range.second)
                        viewModel.toggleDatePicker(false)
                    },
                    unselectableDates = emptySet(),
                    noDateBeforeTaday = false,
                    dateInput = true
                ) { viewModel.toggleDatePicker(false) }
            }
            // 日期筛选带排序
            SortableFilterItem(
                field = "date",
                currentSort = compositeState.sortState.activeSorts.firstOrNull { it.field == "date" },
                onSortChange = { newSort ->
                    if (newSort == null) {
                        viewModel.removeSortsByField("date")
                    } else {
                        viewModel.updateSorts(newSort)
                    }
                }
            ) {
                Row(
                    Modifier
                        .fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${stringResource(R.string.reservation_field_date)}: ")
                    Text("${compositeState.dateState.startDate} ~ ${compositeState.dateState.endDate}")
                    IconButton(
                        onClick = { viewModel.toggleDatePicker(true) }
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.date_picker_dialog_title)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (expanded) stringResource(R.string.reservation_filter_hide) else stringResource(
                        R.string.reservation_filter_more
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { expanded = !expanded }
                ) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.date_picker_dialog_title)
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    // 价格筛选带排序
                    SortableFilterItem(
                        field = "price",
                        currentSort = compositeState.sortState.activeSorts.firstOrNull { it.field == "price" },
                        onSortChange = { newSort ->
                            if (newSort == null) {
                                viewModel.removeSortsByField("price")
                            } else {
                                viewModel.updateSorts(newSort)
                            }
                        }
                    ) {
                        PriceFilter(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .padding(end = 12.dp),
                            currentRange = compositeState.priceState.selectedPrice,
                            fullRange = compositeState.priceState.priceRange,
                            onValueChange = { viewModel.updateSelectedPrice(it) }
                        )
                    }

                    SortableFilterItem(
                        field = "extras",
                        currentSort = compositeState.sortState.activeSorts.firstOrNull { it.field == "extras" },
                        onSortChange = { newSort ->
                            if (newSort == null) {
                                viewModel.removeSortsByField("extras")
                            } else {
                                viewModel.updateSorts(newSort)
                            }
                        }
                    ) {
                        // 服务筛选
                        MultiSelectFilter(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            title = stringResource(R.string.reservation_field_additional_services),
                            options = compositeState.availableServices,
                            selectedOptions = compositeState.serviceState.selectedServices,
                            onSelectionChanged = { selected ->
                                viewModel.updateSelectedServices(selected)
                            }
                        )
                    }

                    SortableFilterItem(
                        field = "roomType",
                        currentSort = compositeState.sortState.activeSorts.firstOrNull { it.field == "roomType" },
                        onSortChange = { newSort ->
                            if (newSort == null) {
                                viewModel.removeSortsByField("roomType")
                            } else {
                                viewModel.updateSorts(newSort)
                            }
                        }
                    ) {  // 房型筛选
                        SingleSelectFilter(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            title = "Tipo de habitación",
                            options = compositeState.availableRoomTypes,
                            selectedOption = compositeState.roomTypeState.selectedRoomType,
                            onSelectionChanged = { viewModel.updateSelectedRoomType(it) }
                        )
                    }
                }
            }
        }
        // 新增筛选标签区域
        ActiveFiltersDisplay(
            startDate = compositeState.dateState.startDate,
            endDate = compositeState.dateState.endDate,
            activeSorts = compositeState.sortState.activeSorts,
            selectedServices = compositeState.serviceState.selectedServices,
            selectedRoomType = compositeState.roomTypeState.selectedRoomType,
            selectedPriceRange = compositeState.priceState.selectedPrice,
            onRemoveSort = { viewModel.removeSort(it) },
            onClearServices = {
                viewModel.clearSelectedServices()
                viewModel.removeSortsByField("extras") // 同时移除相关排序
            },
            onClearRoomType = {
                viewModel.updateSelectedRoomType(null)
                viewModel.removeSortsByField("roomType")
            },
            onClearPrice = {
                viewModel.clearSlectedPrice()
                viewModel.removeSortsByField("price")
            },
            defaultPriceRange = compositeState.priceState.priceRange,
            onClearDates = {
                viewModel.clearDates()
                viewModel.removeSortsByField("date")
            }
        )
    }
}




@Preview(showBackground = true)
@Composable
fun PreviewFilterBar() {
    // 由于 ReservaViewModel 需要传递，我们可以创建一个 Mock 版本来测试
    FilterBar(viewModel = FilterReservaViewModel())
}