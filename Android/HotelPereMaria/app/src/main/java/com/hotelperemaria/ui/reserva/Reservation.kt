package com.hotelperemaria.ui.reserva

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.util.Logger
import com.hotelperemaria.R
import com.hotelperemaria.ui.reserva.components.DatePicker
import com.hotelperemaria.ui.reserva.components.ReservationButton
import com.hotelperemaria.ui.reserva.components.ServicesList
import com.hotelperemaria.ui.reserva.components.Slider
import com.hotelperemaria.ui.reserva.components.TopBar
import com.hotelperemaria.utils.LoadingScreen

@Composable
fun ReservationScreen(reservaViewModel : ReservaViewModel = hiltViewModel()) {
    if (reservaViewModel.showErrorDialog) {
        ErrorDialog(
            message = reservaViewModel.errorMessage,
            onDismiss = { reservaViewModel.showErrorDialog = false }
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopBar(stringResource(R.string.top_bar_reservation)) {
            println("Back")
        }
        RoomInfoCard("Informacion de la habitacion")
        DatePicker(reservaViewModel)
        Slider(stringResource(R.string.reservation_field_guests), reservaViewModel.maxHuesped) {
            reservaViewModel.huespedSelected = it
        }
        ServicesList(reservaViewModel)
        ReservationButton(stringResource(R.string.reservation_button_confirm)) {
            reservaViewModel.confirmReservation()
        }
    }
    LoadingScreen(isLoading = reservaViewModel.isLoading)
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


@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, "Error", tint = MaterialTheme.colorScheme.error) },
        title = { Text(stringResource(R.string.alert_dialog_error_title_missing_data), color = MaterialTheme.colorScheme.error) },
        text = { Text(message, Modifier.fillMaxWidth() ,color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_confirm), color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
@Preview
@Composable
fun ReservationScreenPreview() {
    val reservaViewModel: ReservaViewModel = viewModel()
    ReservationScreen(reservaViewModel)
}