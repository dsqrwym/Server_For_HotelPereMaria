package com.hotelperemaria.ui.principal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hotelperemaria.R
import com.hotelperemaria.data.model.Habitaciones
import com.hotelperemaria.ui.reserva.ReservaViewModel

@Composable
fun PrincipalScreen(viewModel: PrincipalScreenViewModel = hiltViewModel(), onItemClick: () -> Unit) {
    val habitaciones by viewModel.habitaciones.observeAsState(emptyList())
    val nombre by viewModel.nombre.observeAsState("")
    val tiposSeleccionados by viewModel.tiposSeleccionados.observeAsState(emptyList())

    val reservaViewModel: ReservaViewModel = hiltViewModel(LocalContext.current as ComponentActivity)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.fondo))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botón con dropdown para filtros
        FiltrosButton(
            nombre = nombre,
            tiposDisponibles = viewModel.tiposDisponibles,
            tiposSeleccionados = tiposSeleccionados,
            onNombreChange = { nuevoNombre -> viewModel.buscador(nuevoNombre) },
            onTipoSeleccionado = { nuevoTipo -> viewModel.seleccionarTipo(nuevoTipo) }
        )

        // Lista de habitaciones filtradas
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habitaciones) { habitacion ->
                HabitacionCard(habitacion) {
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

@Composable
fun FiltrosButton(
    nombre: String,
    tiposDisponibles: List<String>,
    tiposSeleccionados: List<String>,
    onNombreChange: (String) -> Unit,
    onTipoSeleccionado: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        // Botón para abrir el menú de filtros
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Filtros")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Buscador dentro del dropdown
            OutlinedTextField(
                value = nombre,
                onValueChange = { onNombreChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                label = { Text("Buscar habitación") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )

            // Checkboxes para seleccionar tipos de habitación
            tiposDisponibles.forEach { tipo ->
                val checked = tiposSeleccionados.contains(tipo)
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onTipoSeleccionado(tipo) }
                            )
                            Text(text = tipo)
                        }
                    },
                    onClick = { onTipoSeleccionado(tipo) }
                )
            }
        }
    }
}

// Función para convertir Base64 a Bitmap
fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val cleanBase64 = base64String.replace("\n", "").replace("\r", "")
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: IllegalArgumentException) {
        Log.e("Base64", "Error al decodificar Base64: ${e.message}")
        null
    }
}
