const { Schema, model } = require('mongoose');
const { AuditableSchema, auditablePlugin } = require('./auditableShema.js');

const tipoCapacidadSchema = new Schema({
  tipo: {
    type: String,
    required: true,
    enum: ['individual', 'estándar', 'lujo', 'suite', 'suite dúplex']
  },
  capacidad: {
    type: Number,
    required: true,
    min: [1, 'La capacidad mínima es 1'],
    validate: {
      validator: Number.isInteger,
      message: '{VALUE} no es un número entero válido',
    }
  },
}, { _id: false });

const habitacionSchema = new Schema({
  nombre: {
    type: String,
    required: true,
    trim: true
  },
  descripcion: {
    type: String,
    required: false,
    trim: true
  },
  tipo_capacidad: {
    type: tipoCapacidadSchema,
    required: true
  },
  precio_noche: {
    type: Number,
    required: true,
    min: 1
  },
  extras: [{
    nombre: { type: String, required: true },
    precio: { type: Number, required: true, min: 1 },
    _id: false
  }],
  fotos: [{ type: Schema.Types.ObjectId, ref: 'Imagen' }],
  
  ...AuditableSchema.obj,
});

habitacionSchema.plugin(auditablePlugin);

habitacionSchema.set('toJSON', {
  virtuals: true,
  transform: (doc, ret) => {
    delete ret.id;
    delete ret.desactivado;
    delete ret.offTime;
    delete ret.__v;
    return ret;
  }
});

module.exports = model('Habitacion', habitacionSchema, 'habitaciones');