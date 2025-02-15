package com.hotelperemaria.data.model

enum class TipoImagen {
    user, habitacion, userDefault, habDefault
}

data class Imagen(
    val _id: String,
    private val tipo: String?,
    val image: ByteArray,
    val fecha: String?
){
    val tipoImagen: TipoImagen
        get() = when (tipo) {
            "user" -> TipoImagen.user
            "habitacion" -> TipoImagen.habitacion
            "userDefault" -> TipoImagen.userDefault
            "habDefault" -> TipoImagen.habDefault
            else -> TipoImagen.habDefault
        }
}
