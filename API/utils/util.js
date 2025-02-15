const Habitacion = require('../models/habitacion.js');
const User = require('../models/usuarios.js');


async function checkIdUser(id_usuario, res) {
    if (id_usuario && isMongoId(id_usuario)) {
        const user = await User.findById(id_usuario).select('_id');
        if (!user) {
            res.status(404).json({ error: 'Usuario no encontrado' });
            return false;
        }
    }

    return true;
}

async function checkIdHab(id_habitacion, res) {
    if (id_habitacion && isMongoId(id_habitacion)) {
        const habitacion = await Habitacion.findById(id_habitacion).select('_id');
        if (!habitacion) {
            sendResponse(res, 404, 'Habitación no encontrada');
            return false;
        }
    }

    return true;
}

function handleValidationError(res, error, mensaje) {
    if (error.isJoi) {
        return sendResponse(res, 400, 'Error de validacion', null, error.details.map(d => d.message).join(';'));
    }
    return sendResponse(res, 500, mensaje, null, error.message);
}


function sendResponse(res, statusCode, message, data = null, error = null) {
    const response = {
        statusCode,
        message,
        data,
    };
    if (error) {
        response.error = error;
    }
    return res.status(statusCode).json(response);
};

module.exports = sendResponse;


module.exports = {
    checkIdHab,
    checkIdUser,
    handleValidationError,
    sendResponse
}