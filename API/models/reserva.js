const { Schema, model } = require('mongoose');
const { AuditableSchema, auditablePlugin } = require('./auditableShema.js');
const { isMongoId } = require('validator');

const reservaSchema = new Schema({
  id_usuario: {
    type: Schema.Types.ObjectId,
    ref: 'Usuario',
    required: [true, 'El ID de usuario es obligatorio'],
    validate: {
      validator: (value) => isMongoId(String(value)),
      message: 'El ID de usuario no es válido'
    }
  },
  id_habitacion: {
    type: Schema.Types.ObjectId,
    ref: 'Habitacion',
    required: [true, 'El ID de habitación es obligatorio'],
    validate: {
      validator: (value) => isMongoId(String(value)),
      message: 'El ID de habitación no es válido'
    }
  },
  huespedes: {
    type: Number,
    required: true,
    min: [1, 'El número de huéspedes debe ser al menos 1.']
  },
  fecha_entrada: {
    type: Date,
    required: true
  },
  fecha_salida: {
    type: Date,
    required: true
  },
  precio_total: {
    type: Number,
    min: [0, 'El precio total no debe ser negativo.']
  },
  extrasSeleccionados: [
    {
      nombre: {
        type: String,
        required: [true, 'El nombre del extra es obligatorio']
      },
      precio: {
        type: Number,
        required: [true, 'El precio del extra es obligatorio'],
        min: [0, 'El precio del extra no debe ser negativo.']
      },
      _id: false
    }
  ],
  ...AuditableSchema.obj,
});

reservaSchema.plugin(auditablePlugin);

reservaSchema.index(
  { id_habitacion: 1, fecha_entrada: 1, fecha_salida: 1 },
  { unique: true, partialFilterExpression: { desactivado: false } }
);

reservaSchema.set('toJSON', {
  virtuals: true,
  transform: (doc, ret) => {
    delete ret.id;
    delete ret.desactivado;
    delete ret.offTime;
    delete ret.__v;
    return ret;
  }
});

module.exports = model('Reserva', reservaSchema, 'reservas');