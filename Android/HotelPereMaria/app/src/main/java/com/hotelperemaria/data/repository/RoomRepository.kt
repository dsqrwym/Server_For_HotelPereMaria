package com.hotelperemaria.data.repository

import com.hotelperemaria.data.auth.SessionManager
import com.hotelperemaria.data.model.Habitaciones
import com.hotelperemaria.data.network.ApiHabitacionService
import javax.inject.Inject

class RoomRepository @Inject constructor(private val apiHabitacionService: ApiHabitacionService, private val sessionManager: SessionManager) {
    suspend fun getHabList(): List<Habitaciones> {
        val token = sessionManager.getAccessToken()
        if (token!=null) {
            return apiHabitacionService.listHabs(token)
        }else{
            sessionManager.clearTokens()
            return emptyList()
        }
    }
}