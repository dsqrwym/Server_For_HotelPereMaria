package com.hotelperemaria.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal
import java.time.Instant

object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING) // 使用 String 进行序列化

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString()) // BigDecimal 转 String
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonPrimitive -> BigDecimal(jsonElement.content) // 直接转换成字符串
            else -> throw SerializationException("Invalid precio format")
        }// String 转 BigDecimal
    }
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())  // 将 Instant 转换为字符串（ISO-8601 格式）
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())  // 将字符串转换为 Instant（ISO-8601 格式）
    }
}

enum class RolesForUser {
    CLIENT, EMPLOYEE, ADMIN, SUPERADMIN
}
@Serializable
data class Usuario(
    val _id: String,
    val nombre: String? = null,
    val apellido: String? = null,
    val telefono: String? = null,
    val email: String,
    val NIF: String? = null,
    val username: String,
    val tipo: Int = 1,
    val id_avatar: String? = null
) {
    val displayName: String
        get() = if (!nombre.isNullOrBlank() && !apellido.isNullOrBlank()) {
            "$nombre $apellido"
        } else {
            username
        }

    val role: RolesForUser
        get() = when (tipo) {
            1 -> RolesForUser.CLIENT
            2 -> RolesForUser.EMPLOYEE
            3 -> RolesForUser.ADMIN
            5 -> RolesForUser.SUPERADMIN
            else -> RolesForUser.CLIENT
        }
}

@Serializable
data class TipoCapacidad(
    val tipo: String,  // individual, estándar, lujo, suite, suite dúplex
    val capacidad: Int  // 必须是整数，最小值 1
)

@Serializable
data class Habitacion(
    val _id: String,
    val Tipo: String,
    val Capacidad: Int,
    @SerialName("Precio")
    val precio_noche: Double,
    @SerialName("Extras") @Serializable(with = ExtrasSerializer::class)
    val extras: List<ExtraService> = emptyList(),
    val Images: String
)

object ExtrasSerializer : KSerializer<List<ExtraService>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ExtrasSerializer")

    override fun deserialize(decoder: Decoder): List<ExtraService> {
        val map = decoder.decodeSerializableValue(MapSerializer(String.serializer(), Double.serializer()))
        return map.map { (key, value) -> ExtraService(key, BigDecimal.valueOf(value)) }
    }

    override fun serialize(encoder: Encoder, value: List<ExtraService>) {
        val map = value.associate { it.nombre to it.precio.toDouble() }
        encoder.encodeSerializableValue(MapSerializer(String.serializer(), Double.serializer()), map)
    }
}
@Serializable
data class ExtraService(
    val nombre: String,
    @Serializable(with = BigDecimalSerializer::class)
    val precio: BigDecimal
)

@Serializable
data class ReservaForList(
    val id: String,
    val id_habitacion: Habitacion? = null,
    @Serializable(with = InstantSerializer::class)
    val fecha_entrada: Instant,
    @Serializable(with = InstantSerializer::class)
    val fecha_salida: Instant,
    val huespedes: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val precio_total: BigDecimal,
    val extrasSeleccionados: List<ExtraService>,
) {
    var estado: ReservaEstado = isExpired()
    var tipo_habitacion: String = "Unknown"
    fun isExpired(): ReservaEstado {
        return if (fecha_salida < Instant.now()) ReservaEstado.COMPLETADA else ReservaEstado.ACTIVA
    }
}

enum class ReservaEstado {
    ACTIVA, COMPLETADA
}

// 数据类
@Serializable
data class ReservaResponse(
    val _id: String,
    val id_usuario: Usuario? = null,
    val id_habitacion: Habitacion,
    val huespedes: Int,
    @Serializable(with = InstantSerializer::class)
    val fecha_entrada: Instant? = null,
    @Serializable(with = InstantSerializer::class)
    val fecha_salida: Instant? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val precio_total: BigDecimal? = BigDecimal.ZERO,
    val extrasSeleccionados: List<ExtraService>? = emptyList()
)

@Serializable
data class ReservaRequest(
    val id_usuario: String,
    val id_habitacion: String,
    val huespedes: Int,
    @Serializable(with = InstantSerializer::class)
    val fecha_entrada: Instant,
    @Serializable(with = InstantSerializer::class)
    val fecha_salida: Instant,
    val extrasSeleccionados: List<ExtraService>
)