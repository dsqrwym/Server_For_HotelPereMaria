const { isMongoId } = require('validator');
const User = require('../models/usuarios.js');
const Habitacion = require('../models/habitacion.js');
const { model } = require('mongoose');

async function checkRelatedData(id_usuario, id_habitacion, res) {
    if (id_usuario && isMongoId(id_usuario)) {
        const user = await User.findById(id_usuario).select('_id');
        if (!user) {
            res.status(404).json({ error: 'Usuario no encontrado' });
            return false;
        }
    }

    if (id_habitacion && isMongoId(id_habitacion)) {
        const habitacion = await Habitacion.findById(id_habitacion).select('_id');
        if (!habitacion) {
            res.status(404).json({ error: 'Habitación no encontrada' });
            return false;
        }
    }

    return true;
}

// utils/validateReserva.js

// 验证预约的日期
async function validateFecha(reservaData) {
    if (new Date(reservaData.fecha_entrada) >= new Date(reservaData.fecha_salida)) {
        return { message: 'La fecha de entrada debe ser anterior a la fecha de salida.', statusCode: 400 };
    }
    return null;
}

// 检查日期是否冲突
async function checkDateConflict(reservaData) {
    const conflictingReservations = await model('Reserva').findOne({
        id_habitacion: reservaData.id_habitacion,
        desactivado: false,
        _id: { $ne: reservaData._id }, // 排除当前预约
        $and: [
            { fecha_salida: { $gte: reservaData.fecha_entrada } },
            { fecha_entrada: { $lte: reservaData.fecha_salida } }
        ]
    });

    if (conflictingReservations) {
        return { message: 'El rango de fechas ya está reservado para esta habitación.', statusCode: 409 };
    }

    return null;
}

// 验证房间是否存在
async function checkHabitacionExists(reservaData) {
    const habitacion = await Habitacion.findById(reservaData.id_habitacion);
    if (!habitacion) {
        return { message: 'La habitación no existe.', statusCode: 404 };
    }
    return habitacion;
}

// 验证 extras 是否有效
async function validateExtras(reservaData, habitacion) {
    if (reservaData.extrasSeleccionados) {
        const habitacionExtrasNombres = habitacion.extras.map(extra => extra.nombre);
        const invalidExtras = reservaData.extrasSeleccionados.filter(
            extra => !habitacionExtrasNombres.includes(extra.nombre)
        );

        if (invalidExtras.length > 0) {
            return {
                message: `Los siguientes extras no están disponibles para esta habitación: ${invalidExtras.map(extra => extra.nombre).join(', ')}.`,
                statusCode: 400
            };
        }
    }
    return null;

}

// 验证人数是否超出房间容量
async function validateHuespedes(reservaData, habitacion) {
    if (reservaData.huespedes > habitacion.tipo_capacidad.capacidad) {
        return { message: 'Numero de huesped superado a la capacidad maxima de dicha habitacion', statusCode: 400 };
    }
    return null;
}

// 计算并返回总价格
async function calculatePrecioTotal(reservaData, habitacion) {
    const nights = Math.ceil((new Date(reservaData.fecha_salida) - new Date(reservaData.fecha_entrada)) / (1000 * 60 * 60 * 24));
    const habitacionPrecio = nights * habitacion.precio_noche;
    const extrasPrecio = reservaData.extrasSeleccionados? reservaData.extrasSeleccionados.reduce((total, extra) => total + (extra.precio || 0), 0): 0;
    return habitacionPrecio + extrasPrecio;
}

// 主验证函数
async function validateReserva(reservaData) {
    let habitacion;

    // 1. 验证日期
    const fechaError = await validateFecha(reservaData);
    if (fechaError) return fechaError;
    // 2. 检查日期冲突
    const dateConflictError = await checkDateConflict(reservaData);
    if (dateConflictError) return dateConflictError;
    // 3. 检查房间是否存在
    habitacion = await checkHabitacionExists(reservaData);
    if (!habitacion) return habitacion; // habitacion 是 null，已经处理过
    // 4. 验证 extras
    const extrasError = await validateExtras(reservaData, habitacion);
    if (extrasError) return extrasError;
    // 5. 验证人数
    const huespedesError = await validateHuespedes(reservaData, habitacion);
    if (huespedesError) return huespedesError;
    // 6. 计算并返回总价格
    reservaData.precio_total = await calculatePrecioTotal(reservaData, habitacion);
    return null; // 无错误
}

module.exports = {
    checkRelatedData,
    validateReserva
}