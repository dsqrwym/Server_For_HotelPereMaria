package com.hotelperemaria.ui.reserva

import androidx.activity.ComponentActivity
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hotelperemaria.R
import com.hotelperemaria.ui.principal.HabitacionCard
import com.hotelperemaria.ui.reserva.components.DatePicker
import com.hotelperemaria.ui.reserva.components.ReservationButton
import com.hotelperemaria.ui.reserva.components.ServicesList
import com.hotelperemaria.ui.reserva.components.Slider
import com.hotelperemaria.ui.reserva.components.TopBar
import com.hotelperemaria.utils.ConfirmationDialog
import com.hotelperemaria.utils.LoadingScreen

@Composable
fun ReservationScreen(reservaViewModel : ReservaViewModel = hiltViewModel(LocalContext.current as ComponentActivity), onBack: () -> Unit) {
    val isLoading = reservaViewModel.isLoading.collectAsState()
    val errorMessage by reservaViewModel.errorMessage.collectAsState()
    val habitaciones = reservaViewModel.currentHabitacion.collectAsState()
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
        TopBar(stringResource(R.string.top_bar_reservation)) {
            onBack()
        }

        if (habitaciones.value != null){
            HabitacionCard(habitaciones.value!!) { }
        }
        DatePicker(reservaViewModel)
        val maxim = reservaViewModel.maxHuesped.collectAsState()
        Slider("${stringResource(R.string.reservation_field_guests)}: ", maxim.value) {
            reservaViewModel.updateHuespedSelected(it)
        }
        ServicesList(reservaViewModel)
        ReservationButton(stringResource(R.string.reservation_button_confirm)) {
            reservaViewModel.confirmReservation()
        }
    }
    LoadingScreen(isLoading = isLoading.value)
}

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
        showDialog = true,
        title = stringResource(R.string.alert_dialog_error_title_missing_data),
        message = message,
        icon = Icons.Default.Warning,
        icontTnt = MaterialTheme.colorScheme.error,
        confirmText = stringResource(R.string.dialog_confirm),
        confirmButtonColor = MaterialTheme.colorScheme.primary,
        cancelButtonColor = MaterialTheme.colorScheme.onSurface,
        showCancelButton = false, // 只显示确认按钮
        onDismiss = onDismiss,
        onConfirm = onDismiss // 确认按钮和关闭操作相同
    )
}

@Preview
@Composable
fun ReservationScreenPreview() {
    val reservaViewModel: ReservaViewModel = viewModel()
    ReservationScreen(reservaViewModel){}
}