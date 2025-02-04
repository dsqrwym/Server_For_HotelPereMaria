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
            res.status(404).json({ error: 'Habitación no encontrada' });
            return false;
        }
    }

    return true;
}

function handleValidationError(res, error, mensaje) {
    if (error.isJoi) {
        return res.status(400).json({
            error: 'Error de validación',
            detalles: error.details.map(d => d.message).join('; '),
        });
    }
    res.status(500).json({
        error: mensaje,
        detalles: error.message,
    });
}

module.exports = {
    checkIdHab,
    checkIdUser,
    handleValidationError
}