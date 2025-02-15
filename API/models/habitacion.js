const mongoose = require('mongoose');

// Definir el esquema para Habitacion
const habitacionSchema = new mongoose.Schema({
  Id: { type: String, required: true },
  Tipo: { type: String, required: true },
  Capacidad: { type: Number, required: true },
  Precio: { type: Number, required: true },  // Usamos Number para el precio en formato doble
  Extras: { type: Map, of: Number, default: {} }, // Usamos Number para los extras en formato doble
  Images: { type: String, default: "" }, // Un string para las URLs de imágenes
  Nombre: { type: String, required: true, minlength: 3 },
  Descripccion: { type: String, required: true }
});

// Crear el modelo basado en el esquema
const Habitacion = mongoose.model('Habitacion', habitacionSchema, 'habitaciones');

// Exportar el modelo para poder utilizarlo en otras partes de la aplicación
module.exports = Habitacion;