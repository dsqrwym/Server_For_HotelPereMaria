package com.hotelperemaria.ui.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotelperemaria.ui.reserva.utils.DatePicker
import com.hotelperemaria.ui.reserva.utils.ReservationButton
import com.hotelperemaria.ui.reserva.utils.ServicesList
import com.hotelperemaria.ui.reserva.utils.Slider
import com.hotelperemaria.ui.reserva.utils.TopBar

@Composable
fun ReservationScreen(reservaViewModel : ReservaViewModel = hiltViewModel()) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopBar("Reservacion") {
            println("Back")
        }
        RoomInfoCard("Informacion de la habitacion")
        DatePicker(reservaViewModel)
        Slider("Huésped", reservaViewModel.maxHuesped) {
            reservaViewModel.huespedSelected = it
        }
        ServicesList(reservaViewModel)
        ReservationButton("Confirmar") {

        }
    }
}

@Composable
fun RoomInfoCard(info: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(18.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(38.dp),
                clip = true
            )
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(38.dp)
            )
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
    val reservaViewModel: ReservaViewModel = viewModel()
    ReservationScreen(reservaViewModel)
}