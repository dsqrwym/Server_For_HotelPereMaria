package com.hotelperemaria.ui.reserva.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R

@Composable
fun ReservationButton(name: String, onClick: () -> Unit) {
    Row(
        Modifier
            .padding(18.dp)
            .fillMaxWidth(1f),
        horizontalArrangement = Arrangement.Center
    ) {
        ElevatedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.elevatedButtonColors(containerColor = colorResource(R.color.btnSec))
        ) {
            Text(
                text = name,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview
@Composable
fun ReservationButtonPreview() {
    ReservationButton("Reservar") {
        println("Reservar")
    }
}