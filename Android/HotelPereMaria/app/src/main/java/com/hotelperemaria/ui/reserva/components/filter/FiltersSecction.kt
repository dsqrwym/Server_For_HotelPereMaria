package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.FilterReservaViewModel
import com.hotelperemaria.ui.reserva.SortingOption
import com.hotelperemaria.ui.reserva.components.DateRangePickerModal


@Composable
fun FilterBar(
    viewModel: FilterReservaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(18.dp)
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .shadow(
                    elevation = 10.dp,
                    shape = MaterialTheme.shapes.medium
                )
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.End
        ) {
            if (uiState.showDatePicker) {
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
                currentSort = uiState.activeSorts.firstOrNull { it.field == "date" },
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
                    Text("${uiState.startDate} ~ ${uiState.endDate}")
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
                        currentSort = uiState.activeSorts.firstOrNull { it.field == "price" },
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
                            currentRange = uiState.selectedPriceRange,
                            fullRange = uiState.priceRange,
                            onValueChange = { viewModel.updateSelectedPrice(it) }
                        )
                    }

                    SortableFilterItem(
                        field = "extras",
                        currentSort = uiState.activeSorts.firstOrNull { it.field == "extras" },
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
                            options = uiState.availableServices,
                            selectedOptions = uiState.selectedServices,
                            onSelectionChanged = { selected ->
                                viewModel.updateSelectedServices(selected)
                            }
                        )
                    }

                    SortableFilterItem(
                        field = "roomType",
                        currentSort = uiState.activeSorts.firstOrNull { it.field == "roomType" },
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
                            options = uiState.availableRoomTypes,
                            selectedOption = uiState.selectedRoomType,
                            onSelectionChanged = { viewModel.updateSelectedRoomType(it) }
                        )
                    }
                }
            }
        }
        // 新增筛选标签区域
        ActiveFiltersDisplay(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            activeSorts = uiState.activeSorts,
            selectedServices = uiState.selectedServices,
            selectedRoomType = uiState.selectedRoomType,
            selectedPriceRange = uiState.selectedPriceRange,
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
            defaultPriceRange = uiState.priceRange,
            onClearDates = {
                viewModel.clearDates()
                viewModel.removeSortsByField("date")
            }
        )
    }
}

// 带排序功能的筛选项
@Composable
private fun SortableFilterItem(
    field: String,
    currentSort: SortingOption?,
    onSortChange: (SortingOption?) -> Unit,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()

        SortControl(
            field = field,
            currentSort = currentSort,
            onSortChange = onSortChange
        )
    }


}

// 排序控制组件
@Composable
private fun SortControl(
    field: String,
    currentSort: SortingOption?,
    onSortChange: (SortingOption?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // 升序按钮
        IconButton(
            onClick = {
                if (currentSort?.ascending == true) {
                    // 清除排序
                    onSortChange(null)
                } else {
                    // 设置升序
                    val newSort = SortingOption.fromFieldAndDirection(field, true)
                    onSortChange(newSort)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.sort_ascending),
                tint = if (currentSort?.ascending == true) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 降序按钮
        IconButton(
            onClick = {
                if (currentSort?.ascending == false) {
                    // 清除排序
                    onSortChange(null)
                } else {
                    // 设置降序
                    val newSort = SortingOption.fromFieldAndDirection(field, false)
                    onSortChange(newSort)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.sort_descending),
                tint = if (currentSort?.ascending == false) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// 新增筛选标签组件
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFiltersDisplay(
    startDate: String,
    endDate: String,
    activeSorts: List<SortingOption>,
    selectedServices: List<String>,
    selectedRoomType: String?,
    selectedPriceRange: ClosedFloatingPointRange<Float>,
    onRemoveSort: (SortingOption) -> Unit,
    onClearServices: () -> Unit,
    onClearRoomType: () -> Unit,
    onClearPrice: () -> Unit,
    onClearDates: () -> Unit,
    defaultPriceRange: ClosedFloatingPointRange<Float>
) {
    val items = remember(
        startDate,
        endDate,
        activeSorts,
        selectedServices,
        selectedRoomType,
        selectedPriceRange
    ) {
        buildList {
            // 日期筛选
            if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
                add(FilterItem.DateFilter(startDate, endDate))
            }
            // 排序条件
            activeSorts.forEach { sort ->
                add(FilterItem.SortItem(sort))
            }
            // 服务筛选
            if (selectedServices.isNotEmpty()) {
                add(FilterItem.ServiceFilter(selectedServices))
            }
            // 房型筛选
            selectedRoomType?.let {
                add(FilterItem.RoomTypeFilter(it))
            }
            // 价格筛选
            if (selectedPriceRange != defaultPriceRange) {
                add(FilterItem.PriceFilter(selectedPriceRange))
            }
        }
    }

    if (items.isNotEmpty()) {
        FlowRow(
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth()
                .background(Color.Transparent),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.spacedBy((-10).dp)
        ) {
            items.forEach { item ->
                FilterChip(
                    selected = false,
                    onClick = {
                        when (item) {
                            is FilterItem.DateFilter -> onClearDates()
                            is FilterItem.SortItem -> onRemoveSort(item.sort)
                            is FilterItem.ServiceFilter -> onClearServices()
                            is FilterItem.RoomTypeFilter -> onClearRoomType()
                            is FilterItem.PriceFilter -> onClearPrice()
                        }
                    },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (item) {
                                    is FilterItem.DateFilter -> "${stringResource(R.string.reservation_field_date)}: ${item.displayText}"
                                    is FilterItem.SortItem -> "${getSortingOptionDisplayName(item.sort)}: ${item.displayText}"
                                    else -> item.displayText
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

// 筛选项数据类
private sealed class FilterItem(val displayText: String) {
    data class SortItem(val sort: SortingOption) : FilterItem(
        if (sort.ascending) "↑" else "↓"
    )

    data class ServiceFilter(val services: List<String>) : FilterItem(
        "${services.take(2).joinToString()}${if (services.size > 2) "..." else ""}"
    )

    data class RoomTypeFilter(val type: String) : FilterItem(
        type
    )

    data class PriceFilter(val range: ClosedFloatingPointRange<Float>) : FilterItem(
        "${"%.2f".format(range.start)}€ - ${"%.2f".format(range.endInclusive)}€"
    )

    data class DateFilter(val start: String, val end: String) : FilterItem(
        "$start - $end"
    )
}

@Composable
fun getSortingOptionDisplayName(option: SortingOption): String {
    LocalContext.current
    val fieldName = when (option.field) {
        "price" -> stringResource(R.string.sort_price)
        "date" -> stringResource(R.string.reservation_field_date)
        "roomType" -> stringResource(R.string.sort_room_type)
        "extras" -> stringResource(R.string.reservation_field_additional_services)
        else -> option.field
    }
    return fieldName
}

@Preview(showBackground = true)
@Composable
fun PreviewFilterBar() {
    // 由于 ReservaViewModel 需要传递，我们可以创建一个 Mock 版本来测试
    FilterBar(viewModel = FilterReservaViewModel())
}