const { isMongoId } = require('validator');
const Joi = require('joi');

const allowedFields = Object.keys(require('../../models/reserva').schema.obj);
const allowedUsuarioFields = Object.keys(require('../../models/usuarios').schema.obj);
const allowedHabitacionFields = Object.keys(require('../../models/habitacion').schema.obj);

const crearReservaSchema = Joi.object({
    id_usuario: Joi.string().custom((value, helpers) => {
        if (!isMongoId(value)) {
            return helpers.error('any.invalid', { value });
        }
        return value;
    }).required().messages({
        'any.invalid': 'El id_usuario no es válido.',
    }),
    id_habitacion: Joi.string().custom((value, helpers) => {
        if (!isMongoId(value)) {
            return helpers.error('any.invalid', { value });
        }
        return value;
    }).required().messages({
        'any.invalid': 'El id_habitacion no es válido.',
    }),
    huespedes: Joi.number().integer().min(1).required().messages({
        'number.base': 'El número de huéspedes debe ser un número entero.',
        'number.min': 'El número de huéspedes debe ser al menos 1.',
    }),
    fecha_entrada: Joi.date().iso().required().messages({
        'date.base': 'La fecha de entrada debe ser una fecha válida.',
        'date.format': 'La fecha de entrada debe estar en formato ISO 8601.',
    }),
    fecha_salida: Joi.date().iso().required().messages({
        'date.base': 'La fecha de salida debe ser una fecha válida.',
        'date.format': 'La fecha de salida debe estar en formato ISO 8601.',
    }),
    extrasSeleccionados: Joi.array().items(
        Joi.object({
            nombre: Joi.string().required().messages({
                'string.base': 'El nombre del extra debe ser una cadena de texto.',
                'any.required': 'El nombre del extra es obligatorio.',
            }),
            precio: Joi.number().min(0).required().messages({
                'number.base': 'El precio del extra debe ser un número.',
                'number.min': 'El precio del extra no puede ser negativo.',
                'any.required': 'El precio del extra es obligatorio.',
            }),
        })
    ).optional().messages({
        'array.base': 'Extras seleccionados debe ser un arreglo.',
    }),
});


const dateSchema = Joi.object({
    fecha_entrada: Joi.date().iso().messages({
        'date.base': 'Fecha de entrada debe ser una fecha válida',
        'date.format': 'Fecha de entrada debe estar en formato ISO 8601.',
    }).required(),
    fecha_salida: Joi.date().iso().messages({
        'date.base': 'Fecha de salida debe ser una fecha válida',
        'date.format': 'Fecha de salida debe estar en formato ISO 8601.',
    }).required(),
});

// Obtener todas las reservas activas con soporte para campos específicos
// 获取所有活跃的预订，支持指定字段
const queryAndPaginationSchema = Joi.object({
    id: Joi.string().custom((value, helpers) => {
        if (!isMongoId(value)) {
            return helpers.error('any.invalid', { value });
        }
        return value;
    }).messages({
        'any.invalid': 'El ID proporcionado no es válido.',
    }),
    id_usuario: Joi.string().custom((value, helpers) => {
        if (!isMongoId(value)) {
            return helpers.error('any.invalid', { value });
        }
        return value;
    }),
    id_habitacion: Joi.string().custom((value, helpers) => {
        if (!isMongoId(value)) {
            return helpers.error('any.invalid', { value });
        }
        return value;
    }),
    fecha_entrada: Joi.date().iso().messages({
        'date.base': 'Fecha de entrada debe ser una fecha válida',
        'date.format': 'Fecha de entrada debe estar en formato ISO 8601.',
    }),
    fecha_salida: Joi.date().iso().messages({
        'date.base': 'Fecha de salida debe ser una fecha válida',
        'date.format': 'Fecha de salida debe estar en formato ISO 8601.',
    }),
    precio_min: Joi.number().min(0).messages({
        'number.base': 'Precio mínimo debe ser un número.',
        'number.min': 'Precio mínimo no puede ser negativo.'
    }),
    precio_max: Joi.number().min(0).messages({
        'number.base': 'Precio máximo debe ser un número.',
        'number.min': 'Precio máximo no puede ser negativo.'
    }),
    altTime_start: Joi.date().iso().messages({
        'date.format': 'Fecha de altTime_start debe estar en formato ISO 8601.',
        'date.base': 'Fecha de altTime_start no es válida.'
    }),
    altTime_end: Joi.date().iso().messages({
        'date.format': 'Fecha de altTime_end debe estar en formato ISO 8601.',
        'date.base': 'Fecha de altTime_end no es válida.'
    }),
    fields: Joi.string().custom((value, helpers) => {
        const requestedFields = value.split(',');
        const invalidaFields = requestedFields.filter(field => !allowedFields.includes(field));
        if (invalidaFields.length > 0) {
            return helpers.error('any.invalid', { value: invalidFields.join(', ') });
        }
        return value;
    }, 'Validar campos específicos').messages({
        'any.invalid': 'Campos inválidos especificados: {{#value}}'
    }),
    usuarioFields: Joi.string().custom((value, helpers) => {
        const requestedFields = value.split(',');
        const invalidFields = requestedFields.filter(field => !allowedUsuarioFields.includes(field));
        if (invalidFields.length > 0) {
            return helpers.error('any.invalid', { value: invalidFields.join(', ') });
        }
        return value;
    }, 'Validar campos de usuario').messages({
        'any.invalid': 'Campos inválidos en usuario: {{#value}}'
    }),
    habitacionFields: Joi.string().custom((value, helpers) => {
        const requestedFields = value.split(',');
        const invalidFields = requestedFields.filter(field => !allowedHabitacionFields.includes(field));
        if (invalidFields.length > 0) {
            return helpers.error('any.invalid', { value: invalidFields.join(', ') });
        }
        return value;
    }, 'Validar campos de habitación').messages({
        'any.invalid': 'Campos inválidos en habitación: {{#value}}'
    }),
    page: Joi.number().integer().min(0).default(0).messages({
        'number.base': 'Página debe ser un número entero',
        'number.integer': 'Página debe ser un número entero',
        'number.min': 'Página no puede ser negativa',
    }),
    limit: Joi.number().integer().min(1).max(50).default(10).messages({
        'number.base': 'Límite debe ser un número entero',
        'number.integer': 'Límite debe ser un número entero',
        'number.min': 'Límite debe ser al menos 1',
        'number.max': 'Maximo 50'
    }),
    includeTotal: Joi.boolean().default(false).messages({
        'boolean.base': 'includeTotal debe ser un valor booleano.'
    }),
    includePagination: Joi.boolean().default(false).messages({
        'boolean.base': 'includePagination debe ser un valor booleano.'
    })
});


module.exports = {
    crearReservaSchema,
    queryAndPaginationSchema,
    dateSchema
}