package com.hotelperemaria.ui.reserva.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R

@Composable
fun PriceFilter(
    modifier: Modifier = Modifier,
    currentRange: ClosedFloatingPointRange<Float>,
    fullRange: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    val ( totalSteps) = remember(fullRange) {
        val stepSize = 0.1f
        val totalSteps = ((fullRange.endInclusive - fullRange.start) / stepSize).toInt() - 1
        stepSize to totalSteps
    }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        // 格式化显示价格范围（两位小数）
        Text(
            text = "${stringResource(R.string.reservation_filter_range_price)}: " +
                    "${"%.2f".format(currentRange.start)}€ ~ " +
                    "${"%.2f".format(currentRange.endInclusive)}€",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        RangeSlider(
            value = currentRange,
            onValueChange = onValueChange,
            valueRange = fullRange,
            steps = totalSteps.toInt(),
            modifier = Modifier.padding(vertical = 5.dp),
            colors = SliderDefaults.colors().copy(thumbColor = colorResource(R.color.btnPrin), activeTrackColor = colorResource(R.color.fondo))
        )
    }
}