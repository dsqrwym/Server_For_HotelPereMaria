const Reserva = require('../models/reserva.js');
// Campos validos
const allowedFields = Object.keys(Reserva.schema.obj);
const { crearReservaSchema, queryAndPaginationSchema } = require('../utils/Joi/reservaJoi.js');
const { checkRelatedData, validateReserva } = require('../utils/reservaUtil.js');
const { mongoIdSchema } = require('../utils/Joi/utilJoi.js');
const { handleValidationError } = require('../utils/util.js');

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
            return res.status(validationError.statusCode || 400).json({
                error: validationError.message
            });
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
        res.status(201).json({
            mensaje: 'Reserva creada con éxito',
            reserva: newReserva.toJSON(),
        });
    } catch (error) {
        handleValidationError(res, error, 'Error al crear la reserva');
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

        const { page, limit, includeTotal, includePagination, fields, usuarioFields, habitacionFields } = validatedQuery;

        const queryConditions = buidQueryConditions(validatedQuery);

        const selectFields = fields ? fields.split(',').join(' ') : null;

        // Construir la consulta
        let query = Reserva.find(queryConditions)
            .select(selectFields) // Especificar campos seleccionados / 指定选择的字段
            .skip(page * limit)   // Saltar registros para paginación / 跳过记录以实现分页
            .limit(parseInt(limit)); // Limitar el número de registros por página / 限制每页的记录数

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
        if (includeTotal || includePagination) {
            const totalReservas = await Reserva.countDocuments(queryConditions);
            if (includeTotal) response.total = totalReservas;
            if (includePagination) {
                response.totalPages = Math.ceil(totalReservas / limit);
                response.currentPage = parseInt(page);
                response.limit = parseInt(limit);
            }
        }

        res.status(200).json(response);
    } catch (error) {
        handleValidationError(res, error, 'Error al obtener las reservas');
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
            return res.status(404).json({ error: 'Reserva no encontrada o desactivada' });
        }

        res.status(200).json(reserva);
    } catch (error) {
        handleValidationError(res, error, 'Error al obtener la reserva');
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
            return res.status(404).json({ error: 'Reserva no encontrada' });
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
            return res.status(400).json({ error: 'No se encontraron campos válidos para actualizar' });
        }

        const reservaData = {
            _id: id,
            ...reserva.toObject(),
            ...filteredUpdates
        };

        const validationError = await validateReserva(reservaData);
        if (validationError) {
            return res.status(validationError.statusCode || 400).json({
                error: validationError.message
            });
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
            return res.status(409).json({
                error: 'Conflicto de versión, por favor intente de nuevo',
                currentVersion: _v
            });
        }

        await session.commitTransaction();
        res.status(200).json({
            mensaje: 'Reserva actualizada con éxito',
        });
    } catch (error) {
        await session.abortTransaction();
        handleValidationError(res, error, 'Error al actualizar la reserva');
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
            return res.status(404).json({ error: 'Reserva no encontrada' });
        }
        const _v = reserva.__v;

        const result = await Reserva.updateOne(
            { _id: id, __v: _v, desactivado: false },
            { $set: { desactivado: true, modUser: req.user._id, offTime: new Date() }, $inc: { __v: 1 } }
        );

        if (result.n === 0) {
            return res.status(404).json({ error: 'Reserva no encontrada o no se realizaron cambios' });
        }

        res.status(200).json({
            mensaje: 'Reserva eliminada con éxito',
        });
    } catch (error) {
        res.status(500).json({
            error: 'Error al eliminar la reserva',
            detalles: error.message,
        });
    }
};