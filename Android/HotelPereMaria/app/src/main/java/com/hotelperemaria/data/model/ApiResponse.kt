package com.hotelperemaria.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T? = null,
    val error: String? = null
)
@Serializable
data class ReservaData(
    val reservas: List<ReservaResponse>
)