package com.hotelperemaria.data.model

import com.google.gson.annotations.SerializedName


    data class Habitaciones(
        @SerializedName("_id") val idMongo: String,
        @SerializedName("Id") val id: String,
        @SerializedName("Tipo") val tipo: String,
        @SerializedName("Capacidad") val capacidad: Int,
        @SerializedName("Precio") val precio: Double,
        @SerializedName("Extras") val extras: Map<String, Double>,
        @SerializedName("Images") val images: String,
        @SerializedName("Nombre") val nombre: String,
        @SerializedName("Descripccion") val descripcion: String
    )

