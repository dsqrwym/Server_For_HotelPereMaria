package com.hotelperemaria.data.model

import java.math.BigDecimal
import java.time.Instant

data class ExtraService(
    val nombre: String,
    val precio: BigDecimal
)

data class Reserva(
    val id: String,
    val id_habitacion: String,
    val fecha_entrada: Instant,
    val fecha_salida: Instant,
    val huespedes: Int,
    val precio_total: BigDecimal,
    val extrasSeleccionados: List<ExtraService>,
) {
    var estado: ReservaEstado = isExpired()
    var tipo_habitacion: String = "Nothing"
    fun isExpired(): ReservaEstado {
        return if(fecha_salida < Instant.now()) ReservaEstado.COMPLETADA else ReservaEstado.ACTIVA
    }
}

enum class ReservaEstado {
    ACTIVA, COMPLETADA
}