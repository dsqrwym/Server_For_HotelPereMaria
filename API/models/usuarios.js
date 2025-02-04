const { Schema, model } = require('mongoose');
const Imagen = require('./imagen.js');
const { hash, compare } = require('bcryptjs');
const { isEmail, isMongoId } = require('validator');
const {AuditableSchema, auditablePlugin} = require('./auditableShema.js');

require('dotenv').config();
const PEPPER = process.env.PEPPER;
const SALT_ROUNDS = parseInt(process.env.SALT_ROUNDS, 10) || 10;

const usuarioSchema = new Schema({
    nombre: { type: String, maxlength: 50 },
    apellido: { type: String, maxlength: 50 },
    telefono: { type: String, maxlength: 15 },
    email: {
        type: String,
        required: true,
        unique: true,
        validate: {
            validator: (v) => isEmail(v, { allow_utf8_local_part: true }),
            message: props => `${props.value} is not a valid email!`
        }
    },
    NIF: { type: String, maxlength: 12 },
    username: { type: String, required: true, unique: true, maxlength: 30 },
    password: { type: String, required: true },
    tipo: { type: Number, required: true, enum: [1, 2, 3, 5], default: 1 }, // 1 -> Cliente, 2 -> Empleado, 3 -> Administrador 5 -> SuperAdmin
    id_avatar: {
        type: Schema.Types.ObjectId,
        validate: {
            validator: (v) => isMongoId(String(v)),
            message: '$img ID is not a valid ObjectId!'
        },
        ref: 'Imagen'
    },
    refreshToken: { type: String },
    ...AuditableSchema.obj,
});
usuarioSchema.plugin(auditablePlugin);

usuarioSchema.pre('save', async function (next) {
    try {
        if (!this.id_avatar) {
            const defaultImage = await Imagen.findOne({ tipo: 'userDefault', desactivado: false });
            this.id_avatar = defaultImage ? defaultImage._id : new ObjectId('678ea0ccdd3103253e2bec49');
        }
        if (this.isModified('password')) {
            this.password = await hash(this.password + PEPPER, SALT_ROUNDS);
        }
        next();
    } catch (err) {
        next(err);
    }
});

usuarioSchema.pre('updateOne', async function (next) {
    try {
      if (this._update.id_avatar) {
        const avatarExists = await Imagen.exists({ _id: this._update.id_avatar });
        if (!avatarExists) {
          const defaultImage = await Imagen.findOne({ tipo: 'userDefault', desactivado: false });
          this._update.id_avatar = defaultImage ? defaultImage._id : new ObjectId('678ea0ccdd3103253e2bec49');
        }
      }
      if (this._update.password) {
        this._update.password = await hash(this._update.password + PEPPER, SALT_ROUNDS);
      }
      next();
    } catch (err) {
      next(err);
    }
  });

usuarioSchema.methods.comparePassword = async function (password) {
    try {
        return await compare(password + PEPPER, this.password);
    } catch (error) {
        throw new Error('Password comparison failed.');
    }
};

usuarioSchema.set('toJSON', {
    virtuals: true,
    transform: (doc, ret) => {
      delete ret.password;
      delete ret.refreshtoken;
      delete ret.id;
      delete ret.desactivado;
      delete ret.offTime;
      delete ret.__v;
      return ret;
    }
  });

usuarioSchema.post('save', function (error, doc, next) {
    if (error.name === 'MongoServerError' && error.code === 11000) {
        const field = error.keyPattern ? Object.keys(error.keyPattern).join(', ') : 'Unknown field';
        next(new Error(`The ${field} is already in use.`));
    } else {
        next(error);
    }
});

module.exports = model('Usuario', usuarioSchema, 'usuarios');