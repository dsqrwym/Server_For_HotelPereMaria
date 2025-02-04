package com.hotelperemaria.ui.reserva.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hotelperemaria.R
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.data.model.Reserva
import com.hotelperemaria.data.model.ReservaEstado
import com.hotelperemaria.ui.reserva.ReservationDetail
import com.hotelperemaria.utils.DateUtils
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import kotlin.math.roundToInt

@Composable
fun ReservaCard(
    reserva: Reserva,
    onEdit: (Reserva) -> Unit,
    onCancel: (Reserva) -> Unit
) {
    val maxOffset = -390f // 允许滑动的最大偏移量（负数，向左滑）
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) } // 记录当前卡片偏移量
    var isDragging by remember { mutableStateOf(false) } // 记录是否在滑动
    var showDialog by remember { mutableStateOf(false) } // 是否显示详情对话框

    Box(
        modifier = Modifier
            .clickable(onClick = {
                if (!isDragging) {
                    coroutineScope.launch {
                        offsetX.animateTo(0f, animationSpec = tween(300))
                    }
                }
            })
            .padding(18.dp)
            .shadow(
                elevation = 10.dp,
                shape = MaterialTheme.shapes.medium
            )
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.shapes.medium
            )
            .height(IntrinsicSize.Min)

    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) } // ✅ 使用 lambda 版本，确保状态高效更新
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            coroutineScope.launch {
                                if (offsetX.value < maxOffset / 2) {
                                    // 若滑动超过一半，则完全展开（显示按钮）
                                    offsetX.animateTo(maxOffset, animationSpec = tween(300))
                                } else {
                                    // 若滑动不足一半，则回到初始状态
                                    offsetX.animateTo(0f, animationSpec = tween(300))
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (reserva.estado == ReservaEstado.ACTIVA) {
                                coroutineScope.launch {
                                    // 计算滑动后的位置，并确保它在 [-maxOffset, 0] 范围内
                                    offsetX.snapTo(
                                        (offsetX.value + dragAmount).coerceIn(maxOffset, 0f)
                                    )
                                }
                            }
                        }
                    )
                }
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .let {
                    if (offsetX.value != maxOffset && !isDragging) {
                        it.clickable {
                            showDialog = true
                        } // 只有卡片未完全展开时可点击
                    } else {
                        it
                    }
                },
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.elevatedCardElevation()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = reserva.tipo_habitacion,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${stringResource(R.string.reservation_field_date)}: ${
                            formatInstant(
                                reserva.fecha_entrada
                            )
                        } - ${
                            formatInstant(reserva.fecha_salida)
                        }",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.reservation_field_price)}: ${reserva.precio_total}€",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${stringResource(R.string.reservation_field_state)}: " + if (reserva.estado == ReservaEstado.ACTIVA) stringResource(
                            R.string.reservation_field_state_activated
                        ) else stringResource(R.string.reservation_field_state_completed),
                        color = if (reserva.estado == ReservaEstado.ACTIVA) Color.Blue else Color.Gray
                    )
                }

            }
        }
        if (reserva.estado == ReservaEstado.ACTIVA) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd) // 让按钮靠右对齐
                    .offset {
                        IntOffset(
                            (offsetX.value - maxOffset - 50).roundToInt(),
                            0
                        )
                    } // ✅ 使用 lambda 版本，按钮随滑动变化
            ) {
                IconButton(
                    onClick = { onEdit(reserva) }
                ) {
                    Icon(Icons.Filled.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                }

                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {

                        onCancel(reserva)
                    }
                ) {
                    Icon(Icons.Filled.Clear, "Cancel", tint = MaterialTheme.colorScheme.error)
                }

            }
        }
    }
    if (showDialog) {
        ReservationDetail(reserva) {
            showDialog = false
        }
    }
}


// 时间格式化方法
@Composable
fun formatInstant(instant: Instant): String {
    return DateUtils.convertMillisToLocalDate(instant.toEpochMilli())
}


// 预览 - 预订激活状态
@Preview(showBackground = true)
@Composable
fun ReservaCardPreviewActiveCancel() {
    MaterialTheme {
        ReservaCard(
            reserva = Reserva(
                id = "1",
                id_habitacion = "203",
                fecha_entrada = Instant.now().minusSeconds(86400),
                fecha_salida = Instant.now().plusSeconds(172800), // 后天
                huespedes = 2,
                precio_total = BigDecimal.valueOf(299.99),
                extrasSeleccionados = listOf(
                    ExtraService("WiFi", BigDecimal.valueOf(10)),
                    ExtraService("Desayuno", BigDecimal.valueOf(15))
                )
            ),
            onEdit = {},
            onCancel = {}
        )
    }
}

// 预览 - 预订激活状态
@Preview(showBackground = true)
@Composable
fun ReservaCardPreviewActive() {
    MaterialTheme {
        ReservaCard(
            reserva = Reserva(
                id = "1",
                id_habitacion = "203",
                fecha_entrada = Instant.now().plusSeconds(86400), // 明天
                fecha_salida = Instant.now().plusSeconds(172800), // 后天
                huespedes = 2,
                precio_total = BigDecimal.valueOf(299.99),
                extrasSeleccionados = listOf(
                    ExtraService("WiFi", BigDecimal.valueOf(10)),
                    ExtraService("Desayuno", BigDecimal.valueOf(15))
                )
            ),
            onEdit = {},
            onCancel = {}
        )
    }
}

// 预览 - 预订完成状态（不可滑动）
@Preview(showBackground = true)
@Composable
fun ReservaCardPreviewCompleted() {
    MaterialTheme {
        ReservaCard(
            reserva = Reserva(
                id = "2",
                id_habitacion = "305",
                fecha_entrada = Instant.now().minusSeconds(259200), // 3天前
                fecha_salida = Instant.now().minusSeconds(172800), // 2天前
                huespedes = 1,
                precio_total = BigDecimal.valueOf(199.99),
                extrasSeleccionados = emptyList()
            ),
            onEdit = {},
            onCancel = {}
        )
    }
}
