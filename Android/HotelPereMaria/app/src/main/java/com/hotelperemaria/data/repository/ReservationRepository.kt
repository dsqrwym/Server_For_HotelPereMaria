package com.hotelperemaria.data.repository

import android.util.Log
import com.hotelperemaria.data.auth.SessionManager
import com.hotelperemaria.data.model.ApiResponse
import com.hotelperemaria.data.model.ExtraService
import com.hotelperemaria.data.model.ReservaForList
import com.hotelperemaria.data.model.ReservaRequest
import com.hotelperemaria.data.model.ReservaResponse
import com.hotelperemaria.data.model.ReservedDate
import com.hotelperemaria.data.model.TipoCapacidad
import com.hotelperemaria.data.network.ApiReservaService
import java.io.IOException
import java.time.Instant
import javax.inject.Inject


sealed class ReservationResult<out T> {
    data class Success<out T>(val data: T) : ReservationResult<T>()
    data class Error(val message: String) : ReservationResult<Nothing>()
    data object Unauthorized : ReservationResult<Nothing>()
}

class ReservationRepository @Inject constructor(
    private val apiReservaService: ApiReservaService,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "ReservationRepository"
        private const val DEFAULT_FIELDS =
            "id_habitacion,huespedes,fecha_entrada,fecha_salida,precio_total,extrasSeleccionados"
        private const val DEFAULT_HABITACION_FIELDS = "Tipo,Capacidad,Precio,Images,Extras"
    }

    private suspend fun <T, R> handleApiResponse(
        apiCall: suspend () -> ApiResponse<T>,
        successTransform: (T) -> R
    ): ReservationResult<R> {
        return try {
            val response = apiCall()
            when (response.statusCode) {
                200 -> {
                    response.data?.let {
                        ReservationResult.Success(successTransform(response.data))
                    } ?: ReservationResult.Error("Empty response data")
                }

                404 -> {
                    Log.w(TAG, "No data found (404), returning empty result")
                    ReservationResult.Error("No data found")
                }

                401 -> ReservationResult.Unauthorized
                else -> ReservationResult.Error(response.message)
            }
        } catch (e: Exception) {
            handleException(e, "API call failed")
        }
    }

    private fun handleException(e: Exception, context: String): ReservationResult.Error {
        return when (e) {
            is IOException -> {
                Log.w(TAG, "Network error in $context: ${e.message}")
                ReservationResult.Error("Network connection failed")
            }

            else -> {
                Log.e(TAG, "Error in $context: ${e.stackTraceToString()}")
                ReservationResult.Error("Operation failed: ${e.localizedMessage}")
            }
        }
    }

    suspend fun getReservedDate(
        idHabitacion: String,
        id: String? = null
    ): ReservationResult<List<ReservedDate>> {
        return try {
            val token = sessionManager.getAccessToken() ?: return ReservationResult.Unauthorized
            Log.d("ReservaRepository", "getReservedDate hanitacionId: $idHabitacion, id: $id")
            handleApiResponse(
                apiCall = { apiReservaService.getReservedDaysForRoom(token, idHabitacion, id) },
                successTransform = {
                    Log.d("ReservaRepository", "getReservedDate: $it")
                    Log.d("ReservaRepository", "getReservedDate: ${it.reservedDates}")
                    it.reservedDates
                }
            )
        } catch (e: Exception) {
            handleException(e, "getReservedDate")
        }
    }

    suspend fun getReservaById(
        id: String,
        fields: String = DEFAULT_FIELDS,
        usuarioFields: String? = null,
        habitacionFields: String = DEFAULT_HABITACION_FIELDS
    ): ReservationResult<ReservaResponse> {
        return try {
            val token = sessionManager.getAccessToken() ?: return ReservationResult.Unauthorized

            handleApiResponse(
                apiCall = {
                    apiReservaService.getReservaById(
                        token,
                        id,
                        fields,
                        usuarioFields,
                        habitacionFields
                    )
                },
                successTransform = { it.first() }
            )
        } catch (e: Exception) {
            handleException(e, "getReservaById")
        }
    }

    suspend fun createReserva(
        idHabitacion: String,
        huespedes: Int,
        fechaEntrada: Instant,
        fechaSalida: Instant,
        extrasSeleccionados: List<ExtraService>
    ): ReservationResult<ApiResponse<Unit>> {
        return try {
            val token = sessionManager.getAccessToken() ?: return ReservationResult.Unauthorized
            val user = sessionManager.currentUser.value
            if (user == null) {
                sessionManager.clearTokens()
                return ReservationResult.Error("User not logged in")
            }
            val result = apiReservaService.createReserva(
                token,
                ReservaRequest(
                    id_usuario = user._id,
                    id_habitacion = idHabitacion,
                    huespedes = huespedes,
                    fecha_entrada = fechaEntrada,
                    fecha_salida = fechaSalida,
                    extrasSeleccionados = extrasSeleccionados
                )
            )
            if (result.statusCode == 201) {
                return ReservationResult.Success(result)
            } else {
                return ReservationResult.Error(result.message)
            }

        } catch (e: Exception) {
            handleException(e, "createReserva")
        }
    }

    suspend fun updateReserva(
        id: String,
        idHabitacion: String,
        huespedes: Int,
        fechaEntrada: Instant,
        fechaSalida: Instant,
        extrasSeleccionados: List<ExtraService>
    ): ReservationResult<ApiResponse<Unit>> {
        return try {
            val token = sessionManager.getAccessToken() ?: return ReservationResult.Unauthorized
            val user = sessionManager.currentUser.value
            if (user == null) {
                sessionManager.clearTokens()
                return ReservationResult.Error("User not logged in")
            }
            val result = apiReservaService.updateReserva(
                token,
                id,
                ReservaRequest(
                    id_usuario = user._id,
                    id_habitacion =idHabitacion,
                    huespedes = huespedes,
                    fecha_entrada = fechaEntrada,
                    fecha_salida = fechaSalida,
                    extrasSeleccionados = extrasSeleccionados
                )
            )
            if (result.statusCode == 200) {
                return ReservationResult.Success(result)
            } else {
                return ReservationResult.Error(result.message)
            }

        } catch (e: Exception) {
            handleException(e, "createReserva")
        }
    }

    private fun convertToReservaForList(response: ReservaResponse): ReservaForList? {
        return with(response) {
            if (fecha_entrada == null || fecha_salida == null || precio_total == null
            ) {
                Log.w(TAG, "Incomplete reservation data: $this")
                return null
            }

            ReservaForList(
                id = _id,
                id_habitacion = id_habitacion,
                fecha_entrada = fecha_entrada,
                fecha_salida = fecha_salida,
                huespedes = huespedes,
                precio_total = precio_total,
                extrasSeleccionados = extrasSeleccionados ?: emptyList()
            ).apply {
                tipo_habitacion = id_habitacion.Tipo
            }
        }
    }

    suspend fun getReservasByUsuarioId(): ReservationResult<List<ReservaForList>> {
        return try {
            val user = sessionManager.currentUser.value
            val token = sessionManager.getAccessToken()
            if (user == null || token == null) {
                sessionManager.clearTokens()
                return ReservationResult.Error("User not logged in")
            }

            handleApiResponse(
                apiCall = {
                    apiReservaService.getReservasByUsuarioId(
                        token,
                        user._id,
                        DEFAULT_FIELDS,
                        null,
                        DEFAULT_HABITACION_FIELDS
                    )
                },
                successTransform = { response ->
                    response.reservas.mapNotNull { convertToReservaForList(it) }
                }
            )
        } catch (e: Exception) {
            handleException(e, "getReservasByUsuarioId")
        }
        /*val usuarioFields = null

        return sessionManager.getAccessToken()?.let { token ->
            sessionManager.currentUser.value?._id?.let { user ->
                apiReservaService.getReservasByUsuarioId(
                    token,
                    user,
                    DEFAULT_FIELDS,
                    usuarioFields,
                    DEFAULT_HABITACION_FIELDS
                ).data?.mapNotNull {
                    val idHabitacion = it.id_habitacion
                    val fechaEntrada = it.fecha_entrada
                    val fechaSalida = it.fecha_salida
                    val huespedes = it.huespedes // 默认值
                    val precioTotal = it.precio_total // 默认值
                    val extras = it.extrasSeleccionados ?: emptyList() // 默认值

                    if (idHabitacion != null && fechaEntrada != null && fechaSalida != null && precioTotal != null && huespedes != null) {
                        ReservaForList(
                            id = it._id,
                            id_habitacion = idHabitacion,
                            fecha_entrada = fechaEntrada,
                            fecha_salida = fechaSalida,
                            huespedes = huespedes,
                            precio_total = precioTotal,
                            extrasSeleccionados = extras
                        ).apply {
                            tipo_habitacion = idHabitacion.tipo_capacidad?.tipo ?: "Unknown"
                        }
                    } else {
                        null // 过滤掉不完整的数据，避免异常
                    }
                }
            }
        } ?: emptyList()*/
    }

    suspend fun getExtras(): ReservationResult<List<ExtraService>> {
        return try {
            val token = sessionManager.getAccessToken()
                ?: return ReservationResult.Unauthorized

            handleApiResponse(
                apiCall = { apiReservaService.getExtras(token) },
                successTransform = { it }
            )
        } catch (e: Exception) {
            handleException(e, "getExtras")
        }
    }

    suspend fun getTypes(): ReservationResult<List<TipoCapacidad>> {
        return try {
            val token = sessionManager.getAccessToken()
                ?: return ReservationResult.Unauthorized

            handleApiResponse(
                apiCall = { apiReservaService.getTypes(token) },
                successTransform = { it }
            )
        } catch (e: Exception) {
            handleException(e, "getTypes")
        }
    }

    suspend fun cancelarReserva(id: String): ReservationResult<Unit> {
        return try {
            val token = sessionManager.getAccessToken()
                ?: return ReservationResult.Unauthorized
            handleApiResponse(
                apiCall = { apiReservaService.cancelarReserva(token, id) },
                successTransform = {}
            )
        } catch (e: Exception) {
            handleException(e, "cancelarReserva")
        }
    }
}