package com.hotelperemaria.ui.reserva

import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.hotelperemaria.R
import com.hotelperemaria.ui.principal.HabitacionCard
import com.hotelperemaria.ui.reserva.components.DatePicker
import com.hotelperemaria.ui.reserva.components.ReservationButton
import com.hotelperemaria.ui.reserva.components.ServicesList
import com.hotelperemaria.ui.reserva.components.Slider
import com.hotelperemaria.ui.reserva.components.TopBar
import com.hotelperemaria.utils.LoadingScreen

@Composable
fun ReservationModifyScreen(
    reservaViewModel: ReservaViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    onBack: () -> Unit
) {
    //reservaViewModel.inicialModifyReservaScreen(reserva)
    val isLoading = reservaViewModel.isLoading.collectAsState()
    val errorMessage by reservaViewModel.errorMessage.collectAsState()
    val habitaciones = reservaViewModel.currentHabitacion.collectAsState()
    var showErrorDialog by remember { mutableStateOf(false) }
    val success = reservaViewModel.success.collectAsState()

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
    } else if (success.value){
        SuccessDialog(
            success.value,
            stringResource(R.string.alert_dialog_success_message_reservation_updated),
        ) {
            reservaViewModel.restaurarSuccessState()
            onBack()
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.fondo)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            TopBar(stringResource(R.string.top_bar_reservation_modify)) {
                onBack()
            }
            if (habitaciones.value != null) {
                HabitacionCard(habitaciones.value!!) { }
            }
            DatePicker(reservaViewModel)
            val maxim = reservaViewModel.maxHuesped.collectAsState()
            Slider("${stringResource(R.string.reservation_field_guests)}: ", maxim.value) {
                reservaViewModel.updateHuespedSelected(it)
            }
            ServicesList(reservaViewModel)
            ReservationButton(stringResource(R.string.reservation_button_modify)) {
                reservaViewModel.modifyReservation()
                onBack()
            }
        }
        LoadingScreen(isLoading = isLoading.value)
    }
}