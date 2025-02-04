package com.hotelperemaria.ui.reserva.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R

@Composable
fun Slider(field: String, maxim: Float, onValueChange: (Float) -> Unit) {
    var state by remember { mutableFloatStateOf(1f) }

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
                .padding(horizontal = 18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$field: ",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1.3f),
                maxLines = 1
            )
            Text(
                text = state.toInt().toString(),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            androidx.compose.material3.Slider(
                value = state,
                onValueChange = {
                    onValueChange(it)
                    state = it
                },
                valueRange = 1f..maxim,
                steps = maxim.toInt() - 2,
                modifier = Modifier.weight(3.2f),
                colors = SliderDefaults.colors().copy(thumbColor = colorResource(R.color.btnPrin), activeTrackColor = colorResource(R.color.fondo))
            )
        }
    }
}