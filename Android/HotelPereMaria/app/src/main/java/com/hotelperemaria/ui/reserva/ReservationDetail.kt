package com.hotelperemaria.ui.reserva

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.data.model.Reserva
import com.hotelperemaria.data.model.ReservaEstado
import com.hotelperemaria.ui.reserva.components.formatInstant

@Composable
fun ReservationDetail(reserva: Reserva, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.reservation_details_title)) },
        text = {
            Column(
                Modifier
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState())) {
                InfoRow(
                    label = stringResource(R.string.room_field_type_room),
                    value = reserva.tipo_habitacion
                )
                HorizontalDivider()
                // 日期范围
                InfoRow(
                    label = stringResource(R.string.reservation_field_date),
                    value = "${formatInstant(reserva.fecha_entrada)}\n~\n${formatInstant(reserva.fecha_salida)}"
                )
                HorizontalDivider()
                // 入住人数
                InfoRow(
                    label = stringResource(R.string.reservation_field_guests),
                    value = reserva.huespedes.toString()
                )
                HorizontalDivider()
                // 总价
                InfoRow(
                    label = stringResource(R.string.reservation_field_price),
                    value = "${reserva.precio_total}€"
                )

                // 附加服务
                if (reserva.extrasSeleccionados.isNotEmpty()) {
                    HorizontalDivider()
                    InfoRow(
                        label = stringResource(R.string.reservation_field_additional_services),
                        value = reserva.extrasSeleccionados
                            .joinToString(",\n") { "${it.nombre} (+${it.precio}€)" }

                    )
                }
                HorizontalDivider()
                // 状态
                InfoRow(
                    label = stringResource(R.string.reservation_field_state),
                    value = when (reserva.estado) {
                        ReservaEstado.ACTIVA -> stringResource(R.string.reservation_field_state_activated)
                        ReservaEstado.COMPLETADA -> stringResource(R.string.reservation_field_state_completed)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_confirm))
            }
        }
    )
}

// 表格行组件
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label : ",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .weight(1f)
                .padding(2.dp)
        )
        VerticalDivider()
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(2.dp),
            textAlign = TextAlign.End
        )
    }
}