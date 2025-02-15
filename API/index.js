const express = require('express');
const bodyParser = require('body-parser');
const authRouter = require('./routes/authRoutes.js');
const authAutenRouter = require('./routes/authAutenTokenRoutes.js');
const reservaRouter = require('./routes/reservasRoutes.js');
const imagenRouter = require('./routes/imagenRoutes.js');
const habitacionesRouter = require('./routes/habitacionesRouter.js');
const usuariosRouter = require('./routes/usuariosRoutes.js');
require('./config/database.js');
require('dotenv').config();

const PORT = process.env.PORT || 3000;

const app = express();

//Manejamiento de json
app.use(bodyParser.json());
//no recibimos peticiones enviadas directamente de un formulario, 
//sino que sera todo enviado en json
app.use(bodyParser.urlencoded({ extended: false }));

//Login
app.use('/api/auth', authRouter);
//Register
app.use('/api/auth', authAutenRouter);

//Reservas
app.use('/api/reservas', reservaRouter);

//Habitacion
app.use('/api/habitaciones', habitacionesRouter);

//Imagen
app.use('/api/imagen', imagenRouter);

app.use('/api/usuarios',usuariosRouter);

//app.use('/user', router);
app.listen(PORT, () => {
    console.log(`Server Started at ${PORT}`);
})