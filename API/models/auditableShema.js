const { Schema } = require('mongoose');

//publico
const AuditableSchema = new Schema({
    altUser: { type: Schema.Types.ObjectId, ref: 'Usuario', required: true }, // Usuario que creó el registro
    modUser: { type: Schema.Types.ObjectId, ref: 'Usuario' }, // Usuario que modificó el registro
    altTime: { type: Date }, // Fecha de creación
    modTime: { type: Date }, // Fecha de última modificación
    desactivado: { type: Boolean, default: false }, // Si el usuario está desactivado
    offUser: { type: Schema.Types.ObjectId, ref: 'Usuario' }, // Usuario que desactivó al usuario
    offTime: { type: Date }, // Fecha de desactivación
}, { _id: false, __v: false });

// auditablePlugin.js
function auditablePlugin(schema) {
    // Pre-save hook
    schema.pre('save', function (next) {
        const currentDate = Date.now();
        if (this.isNew) {
            this.altTime = currentDate; // 创建时间
        }

        this.modTime = currentDate; // 修改时间

        if (this.isModified('desactivado') && this.desactivado) {
            this.offTime = currentDate; // 禁用时间
        }
        next();
    });

    // Pre-updateOne hook
    schema.pre('updateOne', function (next) {
        if (!this._update) this._update = {};
        this._update.modTime = Date.now(); // 修改时间
        if (this._update.desactivado) {
            this._update.offTime = Date.now(); // 禁用时间
        }
        next();
    });

    // Pre-updateMany hook
    schema.pre('updateMany', function (next) {
        if (!this._update) this._update = {};
        this._update.modTime = Date.now(); // 修改时间
        if (this._update.desactivado) {
            this._update.offTime = Date.now(); // 禁用时间
        }
        next();
    });

    // Pre-findOneAndUpdate hook
    schema.pre('findOneAndUpdate', function (next) {
        if (!this._update) this._update = {};
        this._update.modTime = Date.now(); // 修改时间
        if (this._update.desactivado) {
            this._update.offTime = Date.now(); // 禁用时间
        }
        next();
    });
}


module.exports = {
    AuditableSchema,
    auditablePlugin,
}
