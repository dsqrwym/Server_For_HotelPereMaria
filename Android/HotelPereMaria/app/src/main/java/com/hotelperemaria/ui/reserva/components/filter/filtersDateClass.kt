package com.hotelperemaria.ui.reserva.components.filter

import com.hotelperemaria.ui.reserva.SortingOption


// 筛选项数据类
sealed class FilterItem(val displayText: String) {
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
