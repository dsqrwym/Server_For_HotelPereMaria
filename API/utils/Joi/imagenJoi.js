const Joi = require('joi');

exports.imagenSchema = Joi.object({
    image: Joi.alternatives().try(
        Joi.binary() // 支持二进制
            .encoding('base64') // 或者以 Base64 编码的二进制
            .label('Binary image'),
        Joi.string()
            .regex(/^data:image\/[a-z]+;base64,/, 'Base64 image') // 验证 Base64 格式的图片字符串
            .label('Base64 image'),
        Joi.string()
            .uri() // 验证 URL 格式
            .label('Image URL')
    ).required(),
    id_habitacion: Joi.string().custom((value, helpers) => {
            if (!isMongoId(value)) {
                return helpers.error('any.invalid', { value });
            }
            return value;
        }).optional().messages({
            'any.invalid': 'El id_habitacion no es válido.',
        }),
});
