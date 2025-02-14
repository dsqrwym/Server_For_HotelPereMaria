const express = require('express');
const router = express.Router();
const Habitacion = require('../models/habitacion');
const { authenticate } = require('../middleware/auth.js');

router.use(authenticate);

// GET /api/habitaciones
// Obtiene todas las habitaciones con filtros opcionales (como Tipo, PrecioMin, etc.)
router.get('/', async (req, res) => {
    try {
        const { Tipo, CapacidadMin, CapacidadMax, Images, Nombre, Descripccion } = req.query;
        let query = {};

        if (Tipo) query['Tipo'] = Tipo;
        if (CapacidadMin) query['Capacidad'] = { $gte: parseInt(CapacidadMin) };
        if (CapacidadMax) query['Capacidad'] = { ...query['Capacidad'], $lte: parseInt(CapacidadMax) }
        if (Nombre) query['Nombre'] = { $regex: new RegExp(Nombre, 'i') }; // Búsqueda flexible
        if (Descripccion) query['Descripccion'] = { $regex: new RegExp(Descripccion, 'i') };

        const habitaciones = await Habitacion.find(query);
        res.status(200).json(habitaciones);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al obtener habitaciones" });
    }
});


// Obtener una habitación por ID
router.get('/:id', async (req, res) => {
    try {
        const habitacion = await Habitacion.findById(req.params.id).populate('Images');
        if (!habitacion || habitacion.desactivado) {
            return res.status(404).json({ error: 'Habitación no encontrada o desactivada' });
        }
        res.status(200).json(habitacion);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al obtener la habitación" });
    }
});

// Crear una nueva habitación
router.post('/', async (req, res) => {
    try {
      // Verificar que el usuario esté autenticado
      if (!req.user || !req.user.id) {
        return res.status(401).json({ error: 'Usuario no autenticado.' });
      }
  
      // Se asume que req.body ya contiene el JSON sin comprimir
      const { Nombre, Descripccion, Tipo, Capacidad, Precio } = req.body;
  
      // Validar el nombre (mínimo 3 caracteres)
      if (!Nombre || Nombre.trim().length < 3) {
        return res.status(400).json({ error: 'El nombre debe tener al menos 3 caracteres.' });
      }
  
      // Validar que la descripción no supere los 500 caracteres, si se envía
      if (Descripccion && Descripccion.trim().length > 500) {
        return res.status(400).json({ error: 'La descripción no puede superar los 500 caracteres.' });
      }
  
      // Validar que se envíe el precio y que sea un número válido
      if (Precio === undefined || Precio === null || Precio === '') {
        return res.status(400).json({ error: 'El campo Precio es obligatorio.' });
      }
      if (isNaN(Precio)) {
        return res.status(400).json({ error: 'El campo Precio debe ser un número válido.' });
      }
  
      // Validar la capacidad si se envía, comprobando que sea numérica
      if (Capacidad !== undefined && Capacidad !== null && Capacidad !== '') {
        if (isNaN(Capacidad)) {
          return res.status(400).json({ error: 'El campo Capacidad debe ser un número válido.' });
        }
      }
  
      // Construir el objeto a guardar, asignando también el usuario que lo crea
      const habitacionData = {
        ...req.body,
        altUser: req.user.id
      };
  
      const habitacion = new Habitacion(habitacionData);
      await habitacion.save();
  
      return res.status(201).json(habitacion);
    } catch (error) {
      console.error('Error al crear la habitación:', error);
      return res.status(500).json({ error: 'Error interno del servidor.' });
    }
  });
  
// Actualizar una habitación
router.put('/:id', async (req, res) => {
    try {
        const { Nombre, Descripccion } = req.body;

        // Validaciones manuales
        if (Nombre && Nombre.trim().length < 3) {
            return res.status(400).json({ error: 'El nombre debe tener al menos 3 caracteres.' });
        }
        if (Descripccion && Descripccion.trim().length > 500) {
            return res.status(400).json({ error: 'La descripción no puede superar los 500 caracteres.' });
        }

        const habitacionActualizada = await Habitacion.findByIdAndUpdate(req.params.id, req.body, { new: true, runValidators: true });
        if (!habitacionActualizada) {
            return res.status(404).json({ error: 'Habitación no encontrada' });
        }
        res.status(200).json(habitacionActualizada);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al actualizar la habitación" });
    }
});


router.delete('/:id', async (req, res) => {
  try {
      const habitacion = await Habitacion.findOneAndDelete({ Id: req.params.id }); // Buscar por "Id" en lugar de "_id"
      
      if (!habitacion) {
          return res.status(404).json({ error: 'Habitación no encontrada' });
      }
      
      res.status(200).json({ message: 'Habitación eliminada correctamente' });
  } catch (error) {
      console.error(error);
      res.status(500).json({ error: "Error al eliminar la habitación" });
  }
});


module.exports = router;