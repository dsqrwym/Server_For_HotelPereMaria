const Usuario = require('../models/usuarios.js');
const { sendResponse, handleValidationError } = require('../utils/util.js');

exports.getClientes = async (req, res) => {
    try {
        // 查询所有 tipo 为 1（Cliente）且未被禁用的用户
        const clientes = await Usuario.find({ tipo: 1, desactivado: false })
            .select('_id nombre email tipo'); // 仅返回前端需要的字段

        return sendResponse(res, 200, 'Clientes obtenidos con éxito', clientes);
    } catch (error) {
        return handleValidationError(res, error, 'Error al obtener los clientes');
    }
};

exports.getEmployee = async (req, res) => {
    try {
        const clientes = await Usuario.find({ tipo: {$in: [2,3,5]}, desactivado: false })
            .select('_id nombre email tipo');

        return sendResponse(res, 200, 'Empleados obtenidos con éxito', clientes);
    } catch (error) {
        return handleValidationError(res, error, 'Error al obtener los clientes');
    }
};