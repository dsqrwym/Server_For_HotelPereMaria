const express = require('express');
const router = express.Router();
const Habitacion = require('../models/habitacion');
const { authenticate } = require('../middleware/auth.js');

router.use(authenticate);

// Obtener todas las habitaciones con filtros
// Obtener todas las habitaciones con filtros
router.get('/', async (req, res) => {
    try {
        const { tipo, capacidadMin, capacidadMax, precioMin, precioMax, nombre, descripcion } = req.query;
        let query = { desactivado: false };

        if (tipo) query['tipo_capacidad.tipo'] = tipo;
        if (capacidadMin) query['tipo_capacidad.capacidad'] = { $gte: parseInt(capacidadMin) };
        if (capacidadMax) query['tipo_capacidad.capacidad'] = { ...query['tipo_capacidad.capacidad'], $lte: parseInt(capacidadMax) };
        if (precioMin) query['precio_noche'] = { $gte: parseFloat(precioMin) };
        if (precioMax) query['precio_noche'] = { ...query['precio_noche'], $lte: parseFloat(precioMax) };
        if (nombre) query['nombre'] = { $regex: new RegExp(nombre, 'i') }; // Búsqueda flexible
        if (descripcion) query['descripcion'] = { $regex: new RegExp(descripcion, 'i') };

        const habitaciones = await Habitacion.find(query).populate('fotos');
        res.status(200).json(habitaciones);
    } catch (error) {
        res.status(500).json({ error: "Error al obtener habitaciones" });
    }
});

// Obtener una habitación por ID
router.get('/:id', async (req, res) => {
    try {
        const habitacion = await Habitacion.findById(req.params.id).populate('fotos');
        if (!habitacion || habitacion.desactivado) {
            return res.status(404).json({ error: 'Habitación no encontrada o desactivada' });
        }
        res.status(200).json(habitacion);
    } catch (error) {
        res.status(500).json({ error: "Error al obtener la habitación" });
    }
});


// Crear una nueva habitación
router.post('/', async (req, res) => {
    try {
        const { nombre, descripcion } = req.body;

        // Validaciones manuales
        if (!nombre || nombre.trim().length < 3) {
            return res.status(400).json({ error: 'El nombre debe tener al menos 3 caracteres.' });
        }
        if (descripcion && descripcion.trim().length > 500) {
            return res.status(400).json({ error: 'La descripción no puede superar los 500 caracteres.' });
        }

        const habitacionData = {
            ...req.body,
            altUser: req.user.id
        };

        const habitacion = new Habitacion(habitacionData);
        await habitacion.save();
        res.status(201).json(habitacion);
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
});

// Actualizar una habitación
router.put('/:id', async (req, res) => {
    try {
        const { nombre, descripcion } = req.body;

        // Validaciones manuales
        if (nombre && nombre.trim().length < 3) {
            return res.status(400).json({ error: 'El nombre debe tener al menos 3 caracteres.' });
        }
        if (descripcion && descripcion.trim().length > 500) {
            return res.status(400).json({ error: 'La descripción no puede superar los 500 caracteres.' });
        }

        const habitacionActualizada = await Habitacion.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
        if (!habitacionActualizada) {
            return res.status(404).json({ error: 'Habitación no encontrada' });
        }
        res.status(200).json(habitacionActualizada);
    } catch (error) {
        res.status(500).json({ error: "Error al actualizar la habitación" });
    }
});

// Eliminar (desactivar) una habitación
router.delete('/:id', async (req, res) => {
    try {
        const habitacion = await Habitacion.findByIdAndUpdate(req.params.id, { desactivado: true }, { new: true });
        if (!habitacion) {
            return res.status(404).json({ error: 'Habitación no encontrada' });
        }
        res.status(200).json({ message: 'Habitación desactivada correctamente' });
    } catch (error) {
        res.status(500).json({ error: "Error al eliminar la habitación" });
    }
});

module.exports = router;