const Reserva = require('../models/reserva.js');
const Habitacion = require('../models/habitacion.js');
// Campos validos
const allowedFields = Object.keys(Reserva.schema.obj);
const { crearReservaSchema, queryAndPaginationSchema, dateSchema } = require('../utils/Joi/reservaJoi.js');
const { checkRelatedData, validateReserva } = require('../utils/reservaUtil.js');
const { mongoIdSchema } = require('../utils/Joi/utilJoi.js');
const { handleValidationError, sendResponse } = require('../utils/util.js');


exports.getHabitacionesDisponibles = async (req, res) => {
    try {
        // 验证查询参数
        const validatedQuery = await dateSchema.validateAsync(req.query);
        const { fecha_entrada, fecha_salida } = validatedQuery;

        // 格式化日期
        const entrada = new Date(fecha_entrada);
        const salida = new Date(fecha_salida);

        // 查找所有房间
        const habitaciones = await Habitacion.find({});

        // 查找已被预订的房间
        const habitacionesReservadas = await Reserva.find({
            $or: [
                { fecha_entrada: { $lte: salida }, fecha_salida: { $gte: entrada } },
                { fecha_entrada: { $gte: entrada }, fecha_salida: { $lte: salida } }
            ]
        }).distinct('id_habitacion');  // 获取在此日期范围内已被预订的房间ID

        // 筛选出没有被预订的房间
        const habitacionesDisponibles = habitaciones.filter(habitacion =>
            !habitacionesReservadas.includes(habitacion._id.toString())
        );

        // 返回空闲房间
        return sendResponse(res, 200, 'Habitaciones disponibles obtenidas con éxito', habitacionesDisponibles);
    } catch (error) {
        return handleValidationError(res, error, 'Error al obtener las habitaciones disponibles');
    }
};

// Crear una nueva reserva
exports.createReserva = async (req, res) => {
    try {
        // Verificar si el usuario y la habitación existen
        const validatedData = await crearReservaSchema.validateAsync(req.body);

        const { id_usuario, id_habitacion, huespedes, fecha_entrada, fecha_salida, extrasSeleccionados } = validatedData;

        const checkResult = await checkRelatedData(id_usuario, id_habitacion, res);
        if (!checkResult) return;

        const reservaData = {
            id_usuario,
            id_habitacion,
            huespedes,
            fecha_entrada,
            fecha_salida,
            extrasSeleccionados
        };


        // 调用 validateReserva 函数进行验证
        const validationError = await validateReserva(reservaData);
        if (validationError) {
            return sendResponse(res, validationError.statusCode || 400, validationError.message);
        }

        const precio_total = reservaData.precio_total;

        const newReserva = new Reserva({
            id_usuario,
            id_habitacion,
            huespedes,
            fecha_entrada,
            fecha_salida,
            precio_total,
            extrasSeleccionados,
            altUser: req.user._id,
        });

        await newReserva.save();
        return sendResponse(res, 201, 'Reserva creada con éxito', newReserva.toJSON());
    } catch (error) {
        return handleValidationError(res, error, 'Error al crear la reserva');
    }
};



const buidQueryConditions = (validatedQuery) => {
    const queryConditions = { desactivado: false }; // Por defecto, excluye reservas desactivadas / 默认排除已禁用的预订

    if (validatedQuery.id_usuario) queryConditions.id_usuario = validatedQuery.id_usuario;
    if (validatedQuery.id_habitacion) queryConditions.id_habitacion = validatedQuery.id_habitacion;
    if (validatedQuery.fecha_entrada) queryConditions.fecha_entrada = { $gte: new Date(validatedQuery.fecha_entrada) };
    if (validatedQuery.fecha_salida) queryConditions.fecha_salida = { $lte: new Date(validatedQuery.fecha_salida) };
    if (validatedQuery.precio_min || validatedQuery.precio_max) {
        queryConditions.precio_total = {};
        if (validatedQuery.precio_min) queryConditions.precio_total.$gte = validatedQuery.precio_min;
        if (validatedQuery.precio_max) queryConditions.precio_total.$lte = validatedQuery.precio_max;
    }
    if (validatedQuery.altTime_start || validatedQuery.altTime_end) {
        queryConditions.altTime = {};
        if (validatedQuery.altTime_start) queryConditions.altTime.$gte = new Date(validatedQuery.altTime_start);
        if (validatedQuery.altTime_end) queryConditions.altTime.$lte = new Date(validatedQuery.altTime_end);
    }

    return queryConditions;
};

exports.getReservas = async (req, res) => {
    try {

        const validatedQuery = await queryAndPaginationSchema.validateAsync(req.query);

        const { page, limit, includeTotal, includePagination, fields, usuarioFields, habitacionFields, pagination } = validatedQuery;

        const queryConditions = buidQueryConditions(validatedQuery);

        const selectFields = fields ? fields.split(',').join(' ') : null;



        // Construir la consulta
        let query;
        if (pagination) {
            query = Reserva.find(queryConditions)
                .select(selectFields) // Especificar campos seleccionados / 指定选择的字段
                .skip(page * limit)   // Saltar registros para paginación / 跳过记录以实现分页
                .limit(parseInt(limit)); // Limitar el número de registros por página / 限制每页的记录数
        }else{
            query = Reserva.find(queryConditions)
            .select(selectFields); // Especificar campos seleccionados / 指定选择的字段
        }
        // 只有在请求相关字段时才添加 populate
        if (!fields || fields.includes('id_usuario')) {
            query = query.populate({
                path: 'id_usuario',
                select: usuarioFields ? usuarioFields.split(',').join(' ') : null  // 动态选择 usuario 字段
            });
        }

        if (!fields || fields.includes('id_habitacion')) {
            query = query.populate({
                path: 'id_habitacion',
                select: habitacionFields ? habitacionFields.split(',').join(' ') : null  // 动态选择 habitacion 字段
            });
        }


        // 执行查询
        const reservas = await query.exec();
        const response = { reservas };
        if (pagination && (includeTotal || includePagination)) {
            const totalReservas = await Reserva.countDocuments(queryConditions);
            if (includeTotal) response.total = totalReservas;
            if (includePagination) {
                response.totalPages = Math.ceil(totalReservas / limit);
                response.currentPage = parseInt(page);
                response.limit = parseInt(limit);
            }
        }

        return sendResponse(res, 200, 'Reservas obtenidas con éxito', response);
    } catch (error) {
        return handleValidationError(res, error, 'Error al obtener las reservas');
    }
};

// Obtener una reserva por ID
exports.getReservaById = async (req, res) => {
    try {
        const { id } = req.params;

        await mongoIdSchema.validateAsync({ id });

        const { fields, usuarioFields, habitacionFields } = req.query;
        await queryAndPaginationSchema.validateAsync({ id, fields, usuarioFields, habitacionFields });
        const selectFields = fields ? fields.split(',').join(' ') : null;


        // 查询预订
        let query = Reserva.find({ _id: id, desactivado: false }).select(selectFields);


        if (!fields || fields.includes('id_usuario')) {
            query = query.populate({
                path: 'id_usuario',
                select: usuarioFields ? usuarioFields.split(',').join(' ') : null  // 动态选择 usuario 字段
            });
        }


        if (!fields || fields.includes('id_habitacion')) {
            query = query.populate({
                path: 'id_habitacion',
                select: habitacionFields ? habitacionFields.split(',').join(' ') : null  // 动态选择 habitacion 字段
            });
        }

        const reserva = await query.exec();

        if (!reserva || reserva.desactivado) {
            return sendResponse(res, 404, 'Reserva no encontrada o desactivada');
        }

        return sendResponse(res, 200, 'Reserva obtenida con éxito', reserva);
    } catch (error) {
        return handleValidationError(res, error, 'Error al obtener la reserva');
    }
};

// Actualizar una reserva
exports.updateReserva = async (req, res) => {
    const session = await require('mongoose').startSession();
    try {
        session.startTransaction();
        const { id } = req.params;

        await mongoIdSchema.validateAsync({ id });

        const reserva = await Reserva.findById(id);
        if (!reserva) {
            return sendResponse(res, 404, 'Reserva no encontrada o desactivada');
        }
        const _v = reserva.__v;
        const updates = req.body;

        //禁止id_usuario 和 id_habitacion
        const disallowedFields = ['id_usuario', 'id_habitacion'];
        const filteredUpdates = Object.keys(updates).reduce((acc, key) => {
            if (allowedFields.includes(key) && !disallowedFields.includes(key)) {
                acc[key] = updates[key];
            }
            return acc;
        }, {});

        if (Object.keys(filteredUpdates).length === 0) {
            return sendResponse(res, 400, 'No se encontraron campos válidos para actualizar')
        }

        const reservaData = {
            _id: id,
            ...reserva.toObject(),
            ...filteredUpdates
        };

        const validationError = await validateReserva(reservaData);
        if (validationError) {
            return sendResponse(res, validationError.statusCode || 400, validationError.message);
        }

        // Agregar modUser para seguimiento del usuario que realiza la actualización
        filteredUpdates.modUser = req.user._id;
        filteredUpdates.precio_total = reservaData.precio_total

        const result = await Reserva.updateOne(
            { _id: id, __v: _v, desactivado: false },
            { $set: filteredUpdates, $inc: { __v: 1 } },
            { session }
        );

        if (result.n === 0) {
            return sendResponse(res, 404, 'Reserva no encontrada o no se realizaron cambios');
        }

        await session.commitTransaction();
        return sendResponse(res, 200, 'Reserva actualizada con éxito');
    } catch (error) {
        await session.abortTransaction();
        return handleValidationError(res, error, 'Error al actualizar la reserva');
    } finally {
        session.endSession();
    }
};

// Eliminar (desactivar) una reserva
exports.deleteReserva = async (req, res) => {
    try {
        const { id } = req.params;

        await mongoIdSchema.validateAsync({ id });

        const reserva = await Reserva.findById(id).select('_v');
        if (!reserva) {
            return sendResponse(res, 404, 'Reserva no encontrada');
        }
        const _v = reserva.__v;

        const result = await Reserva.updateOne(
            { _id: id, __v: _v, desactivado: false },
            { $set: { desactivado: true, modUser: req.user._id, offTime: new Date() }, $inc: { __v: 1 } }
        );

        if (result.n === 0) {
            return sendResponse(res, 404, 'Reserva no encontrada o no se realizaron cambios');
        }

        return sendResponse(res, 200, 'Reserva eliminada con éxito');
    } catch (error) {
        return sendResponse(res, 500, 'Error al eliminar la reserva', null, error.message);
    }
};

// Eliminar (desactivar) su reserva
exports.deleteSelfReserva = async (req, res) => {
    try {
        const { id } = req.params;
        const usuarioId = req.user._id.toString(); // 确保是字符串

        await mongoIdSchema.validateAsync({ id });

        // 查找预约
        const reserva = await Reserva.findById(id).select('id_usuario __v desactivado');
        if (!reserva) {
            return sendResponse(res, 404, 'Reserva no encontrada');
        }

        if (reserva.id_usuario.toString() !== usuarioId) {
            return sendResponse(res, 403, 'No tienes permiso para cancelar esta reserva');
        }

        if (reserva.desactivado) {
            return sendResponse(res, 400, 'Esta reserva ya ha sido cancelada');
        }

        const _v = reserva.__v;

        const result = await Reserva.updateOne(
            { _id: id, __v: _v, desactivado: false },
            { $set: { desactivado: true, modUser: usuarioId, offTime: new Date() }, $inc: { __v: 1 } }
        );

        if (result.modifiedCount === 0) {
            return sendResponse(res, 404, 'Reserva no encontrada o no se realizaron cambios');
        }

        return sendResponse(res, 200, 'Reserva eliminada con éxito');

    } catch (error) {
        return sendResponse(res, 500, 'Error al eliminar la reserva', null, error.message);
    }
};

// 获取房间已被预约的所有日期，排除特定的预约日期
exports.getReservedDaysForRoom = async (req, res) => {
    try {
        const { id_habitacion, id } = req.query;

        // 验证房间ID是否有效
        await mongoIdSchema.validateAsync({ id: id_habitacion });

        let reservas = null;

        if (id) {
            await mongoIdSchema.validateAsync({ id });
            reservas = await Reserva.find({
                id_habitacion,
                desactivado: false,
                _id: { $ne: id } // 排除指定的 reservaId
            });;
        } else {
            reservas = await Reserva.find({
                id_habitacion,
                desactivado: false
            });;
        }

        let reservedDates = [];

        reservas.forEach(reserva => {
            const startDate = new Date(reserva.fecha_entrada);
            const endDate = new Date(reserva.fecha_salida);
            reservedDates.push({
                fecha_entrada: startDate.toISOString().split('T')[0],
                fecha_salida: endDate.toISOString().split('T')[0]
            });
        });

        return sendResponse(res, 200, 'Obtenido fecha con exito', { reservedDates });

    } catch (error) {
        return handleValidationError(res, error, 'Fallado al tener fecha');
    }
};