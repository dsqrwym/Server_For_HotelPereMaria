package com.hotelperemaria.ui.principal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hotelperemaria.R
import com.hotelperemaria.data.model.Habitaciones
import com.hotelperemaria.ui.reserva.ReservaViewModel

@Composable
fun PrincipalScreen(viewModel: PrinciapalScreenViewModel = hiltViewModel(), onItemClick: () -> Unit) {
    val habitaciones = viewModel.habitaciones.observeAsState(initial = emptyList())

    Box(
        Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.fondo)),
        contentAlignment = Alignment.Center
    ) {

        val reservaViewModel: ReservaViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habitaciones.value) { habitacion ->
                HabitacionCard(habitacion){
                    reservaViewModel.creacionDeReserva(habitacion)
                    onItemClick()
                }
            }
        }
    }
}

@Composable
fun HabitacionCard(habitacion: Habitaciones, onItemClick: (Habitaciones) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick(habitacion) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            habitacion.images?.let { base64String ->
                Log.d("Comp", "Base64: $base64String")
                val bitmap = base64ToBitmap(base64String)
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Imagen de la habitación",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    )
                }
            }

            Text(text = "Nombre: ${habitacion.nombre}")
            Text(text = "Tipo: ${habitacion.tipo}")
            Text(text = "Capacidad: ${habitacion.capacidad} personas")
            Text(text = "Precio: ${habitacion.precio} €")
        }
    }
}

// Función para convertir Base64 a Bitmap
fun base64ToBitmap(base64String: String): Bitmap? {
    try {
        // Limpiar la cadena Base64 eliminando saltos de línea u otros caracteres no válidos
        val cleanBase64 = base64String.replace("\n", "").replace("\r", "")

        // Decodificar la cadena Base64 limpia
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)

        // Convertir los bytes decodificados a Bitmap
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        Log.e("Base64", "Error al decodificar Base64: ${e.message}")
        return null
    }
}