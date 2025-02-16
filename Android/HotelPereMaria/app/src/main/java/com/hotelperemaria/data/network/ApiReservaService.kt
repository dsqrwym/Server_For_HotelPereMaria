package com.hotelperemaria.data.network

import com.hotelperemaria.data.model.ApiResponse
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.data.model.ReservaData
import com.hotelperemaria.data.model.ReservaRequest
import com.hotelperemaria.data.model.ReservaResponse
import com.hotelperemaria.data.model.ReservedDatesData
import com.hotelperemaria.data.model.TipoCapacidad
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiReservaService {
    @GET("reservas/{id}")
    suspend fun getReservaById(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Query("fields") fields: String? = null,
        @Query("usuarioFields") usuarioFields: String? = null,
        @Query("habitacionFields") habitacionFields: String? = null
    ): ApiResponse<List<ReservaResponse>>

    @POST("reservas/")
    suspend fun createReserva(
        @Header("Authorization") authToken: String,
        @Body reserva: ReservaRequest
    ): ApiResponse<Unit>

    @PUT("reservas/{id}")
    suspend fun updateReserva(
        @Header("Authorization") authToken: String,
        @Path("id") id: String,
        @Body reserva: ReservaRequest
    ): ApiResponse<Unit>

    @GET("reservas/")
    suspend fun getReservasByUsuarioId(
        @Header("Authorization") authToken: String,
        @Query("id_usuario") idUsuario: String,
        @Query("fields") fields: String? = null,
        @Query("usuarioFields") usuarioFields: String? = null,
        @Query("habitacionFields") habitacionFields: String? = null
    ): ApiResponse<ReservaData>

    //Obtener infromacion sobre extras, tipos para filtro
    @GET("habitacion/extras")
    suspend fun getExtras(@Header("Authorization") authToken: String,): ApiResponse<List<ExtraService>>

    @GET("habitacion/types")
    suspend fun getTypes(@Header("Authorization") authToken: String,): ApiResponse<List<TipoCapacidad>>

    @GET("reservas/date/")
    suspend fun getReservedDaysForRoom(
        @Header("Authorization") authToken: String,
        @Query("id_habitacion") idHabitacion: String,
        @Query("id") id: String? = null
    ): ApiResponse<ReservedDatesData>

    @DELETE("reservas/cancelar/{id}")
    suspend fun cancelarReserva(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): ApiResponse<Unit>

}
