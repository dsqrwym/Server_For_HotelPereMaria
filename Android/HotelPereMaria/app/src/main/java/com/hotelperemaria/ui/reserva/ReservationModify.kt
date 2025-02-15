package com.hotelperemaria.ui.reserva

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.hotelperemaria.R
import com.hotelperemaria.data.model.ReservaForList
import com.hotelperemaria.ui.reserva.components.DatePicker
import com.hotelperemaria.ui.reserva.components.ReservationButton
import com.hotelperemaria.ui.reserva.components.ServicesList
import com.hotelperemaria.ui.reserva.components.Slider
import com.hotelperemaria.ui.reserva.components.TopBar
import com.hotelperemaria.utils.LoadingScreen
import java.time.Instant

@Composable
fun ReservationModifyScreen(reservaViewModel : ReservaViewModel = hiltViewModel(), reserva: ReservaForList) {
    reservaViewModel.inicialModifyReservaScreen(reserva)
    val isLoading = reservaViewModel.isLoading.collectAsState()
    val errorMessage by reservaViewModel.errorMessage.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }

    // 当 errorMessage 变化时更新对话框状态
    LaunchedEffect(errorMessage) {
        showErrorDialog = errorMessage.isNotEmpty()
    }

    if (showErrorDialog) {
        ErrorDialog(
            message = errorMessage,
            onDismiss = {
                showErrorDialog = false
                reservaViewModel.clearErrorMessage()
            }
        )
    }
    Column(
        Modifier
            .fillMaxSize()
            .background(colorResource(R.color.fondo)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        TopBar(stringResource(R.string.top_bar_reservation_modify)) {
            println("Back")
        }
        RoomInfoCard("Informacion de la habitacion")
        DatePicker(reservaViewModel)
        Slider("${stringResource(R.string.reservation_field_guests)}: ", reservaViewModel.maxHuesped) {
            reservaViewModel.updateHuespedSelected(it)
        }
        ServicesList(reservaViewModel)
        ReservationButton(stringResource(R.string.reservation_button_modify)) {
            reservaViewModel.modifyReservation()
        }
    }
    LoadingScreen(isLoading = isLoading.value)
}

@Preview
@Composable
fun ReservationModifyScreenPreview() {
   // ReservationModifyScreen(reservaForList = ReservaForList(id = "", id_habitacion = "", fecha_salida = Instant.now(), fecha_entrada = Instant.now(), extrasSeleccionados = emptyList(), huespedes = 1, precio_total = 0.toBigDecimal()))
}