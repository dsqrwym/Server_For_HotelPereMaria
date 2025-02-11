const mongoose = require('mongoose');

// Definir el esquema para Habitacion
const habitacionSchema = new mongoose.Schema({
  Id: { type: String, required: true },
  Tipo: { type: String, required: true },
  Capacidad: { type: Number, required: true },
  Precio: { type: mongoose.Types.Decimal128, required: true },  // Utilizamos Decimal128 para manejar precios con decimales
  Extras: { type: Map, of: mongoose.Types.Decimal128, default: {} }, // Un mapa de strings a decimales
  Images: { type: [String], default: [] }, // Un array de strings para las URLs de imágenes
  Nombre: { type: String, required: true, minlength: 3 },
  Descripccion: { type: String, required: true }
});

// Crear el modelo basado en el esquema
const Habitacion = mongoose.model('Habitacion', habitacionSchema);

// Exportar el modelo para poder utilizarlo en otras partes de la aplicación
module.exports = Habitacion;