package com.hotelperemaria.ui.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hotelperemaria.ui.reserva.utils.DatePicker
import com.hotelperemaria.ui.reserva.utils.TopBar

@Composable
fun ReservationScreen(){
    Column(
        Modifier.
        fillMaxSize().
        background(MaterialTheme.colorScheme.background)
    ) {
        TopBar("Reservacion"){
            println("Back")
        }
        Spacer(Modifier.padding(16.dp))
        RoomInfoCard("Informacion de la habitacion")
    }
}

@Composable
fun RoomInfoCard(info: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(38.dp))
    ) {
        Text(
            modifier = Modifier.padding(80.dp),
            text = info,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
fun ReservationScreenPreview() {
    ReservationScreen()
}