const { Schema, model } = require('mongoose');
const { AuditableSchema, auditablePlugin } = require('./auditableShema.js');

const imagenSchema = new Schema({
  fecha: { type: Date, default: Date.now }, // fecha de subida o creacion
  tipo: { type: String, required: true, enum: ['user', 'habitacion', 'userDefault', 'habDefault'] }, // De usuario, de habitacion
  image: { type: Schema.Types.Mixed, required: true }, // URL o Base64
  ...AuditableSchema.obj,
});
imagenSchema.plugin(auditablePlugin);

imagenSchema.index({ tipo: 1 });

imagenSchema.set('toJSON', {
  virtuals: true,       // 启用虚拟字段
  transform: (doc, ret) => {
    delete ret.id;
    delete ret.fecha;
    delete ret.desactivado;
    delete ret.offTime;
    delete ret.__v;
    return ret;
  }
});


module.exports = model('Imagen', imagenSchema, 'imagenes');