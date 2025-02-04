const Joi = require('joi');
const { isMongoId } = require('validator');
exports.mongoIdSchema = Joi.object({
    id: Joi.string().custom((value, helpers) => {
        if (!isMongoId(value)) {
            return helpers.error('any.invalid', { value });
        }
        return value;
    }).required().messages({
        'any.invalid': 'El ID proporcionado no es válido.',
    })
});

exports.mongoIdArraySchema = Joi.object({
    fotos: Joi.string()
        .custom((value, helpers) => {
            const ids = value.split(',').map(id => id.trim());
            if (ids.some(id => !mongoose.Types.ObjectId.isValid(id))) {
                return helpers.error('any.invalid', { value });
            }
            return ids;
        })
        .required()
        .messages({
            'string.base': 'fotos debe ser una cadena de texto con IDs separados por comas',
            'any.invalid': 'Cada ID en fotos debe ser un ObjectId válido de 24 caracteres',
            'any.required': 'Se deben proporcionar los IDs de las fotos en un formato válido',
        }),
});