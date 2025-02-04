const Imagen = require('../models/imagen.js');
const Habitacion = require('../models/habitacion.js');
const { imagenSchema } = require('../utils/Joi/imagenJoi.js');
const { mongoIdArraySchema, mongoIdSchema } = require('../utils/Joi/utilJoi.js');
const { handleValidationError } = require('../utils/util.js');


async function uploadImagen(req, res, tipo) {
    try {
        const { image } = await imagenSchema.validateAsync(req.body);

        const newImagen = new Imagen({
            tipo,
            image,
            altUser: req.user._id,
        });

        await newImagen.save();
        res.status(201).json({
            mensaje: 'Imagen subida con éxito',
            id_imagen: newImagen.id,
        });
    } catch (error) {
        handleValidationError(res, error, 'Error al subir imagen');
    }
}

exports.subirDefautImagen = async (req, res) => {
    uploadImagen(req, res, 'userDefault');
}


exports.subirDefaultHab = async (req, res) => {
    uploadImagen(req, res, 'habDefault');
}

exports.subirUserImagen = async (req, res) => {
    uploadImagen(req, res, 'user');
}


exports.subirHabitacionImagen = async (req, res) => {
    const session = await require('mongoose').startSession();
    session.startTransaction();

    try {
        const { image, id_habitacion } = await imagenSchema.validateAsync(req.body);

        const habitacion = await Habitacion.findById(id_habitacion).session(session);
        if (!habitacion) {
            return res.status(404).json({
                error: 'Habitación no encontrada',
            });
        }

        const newImagen = new Imagen({
            tipo: 'habitacion',
            image,
            altUser: req.user._id,
        });

        const currentVersion = habitacion.__v;

        await newImagen.save({ session });

        const updateResult = await Habitacion.updateOne(
            { _id: id_habitacion, __v: currentVersion },
            {
                $push: { fotos: newImagen._id },
                $set: { modUser: req.user._id },
                $inc: { __v: 1 },
            },
            { session }
        );

        if (updateResult.modifiedCount === 0) {
            return res.status(409).json({
                error: 'Conflicto de versión, por favor intente de nuevo',
            });
        }

        await session.commitTransaction();

        res.status(201).json({
            mensaje: 'Imagen subida con éxito',
            id_imagen: newImagen._id,
        });

    } catch (error) {
        await session.abortTransaction();

        handleValidationError(res, error, 'Error al subir imagen');
    } finally {
        session.endSession();
    }
};


exports.obtenerFotosHabitacion = async (req, res) => {
    try {
        const { fotos } = await mongoIdArraySchema.validateAsync(req.query);

        const imagenes = await Imagen.find({ _id: { $in: fotos }, tipo: 'habitacion' }).select('image');

        if (imagenes.length === 0) {
            return res.status(404).json({
                error: 'No se encontraron imágenes con los IDs proporcionados',
            });
        }

        res.status(200).json(imagenes);
    } catch (error) {
        handleValidationError(res, error, 'Error al obtener imágenes');
    }
};

async function getImagenById(req, res, roles) {
    try {
        const { id } = req.params
        await mongoIdSchema.validateAsync({ id });

        const imagen = await Imagen.findOne({ _id: id, tipo: { $in: roles } }).select('image');;

        if (!imagen) {
            return res.status(404).json({
                error: 'No se encontraron imágene con los ID proporcionado',
            });
        }

        res.status(200).json(imagen);
    } catch (error) {
        handleValidationError(res, error, 'Error al obtener imágen');
    }
};

exports.getUserImagen = async (req, res) => {
    getImagenById(req, res, ['user', 'userDefault']);
};

exports.getHabImagen = async (req, res) => {
    getImagenById(req, res, ['habitacion', 'habDefault']);
};


async function deleteImagenById(req, res, roles) {
    try {

        const { id } = req.params
        await mongoIdSchema.validateAsync({ id });

        const imagen = await Imagen.findById(id).select('_v');
        if (!imagen) {
            return res.status(404).json({ error: 'Imagen no encontrada' });
        }
        const _v = imagen.__v;

        const result = await Imagen.updateOne(
            { _id: id, __v: _v, desactivado: false, tipo: { $in: roles } },
            { $set: { desactivado: true, modUser: req.user._id, offTime: new Date() }, $inc: { __v: 1 } }
        );

        if (result.n === 0) {
            return res.status(404).json({ error: 'Conflicto de versión o imagen ya desactivada' });
        }

        res.status(200).json({
            mensaje: 'Reserva eliminada con éxito',
        });

        res.status(200).json(imagen);
    } catch (error) {
        handleValidationError(res, error, 'Error al eliminar imagen');
    }
};


module.exports.deleteImagenById = async (req, res) => {
    deleteImagenById(req, res, ['user']);
}

module.exports.deleteHabImagenById = async (req, res) => {
    deleteImagenById(req, res, ['habitacion']);
}

module.exports.deleteDefaultImagenById = async (req, res) => {
    deleteImagenById(req, res, ['userDefault', 'habitacionDefault']);
}