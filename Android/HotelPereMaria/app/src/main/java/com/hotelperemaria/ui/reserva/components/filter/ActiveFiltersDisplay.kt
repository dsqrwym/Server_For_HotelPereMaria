package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.SortingOption

// 新增筛选标签组件
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFiltersDisplay(
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