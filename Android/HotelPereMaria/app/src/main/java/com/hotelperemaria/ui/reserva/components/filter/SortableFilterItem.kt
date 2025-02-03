package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.SortingOption


// 带排序功能的筛选项
@Composable
fun SortableFilterItem(
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
