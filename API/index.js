const express = require('express');
const bodyParser = require('body-parser');
const authRouter = require('./routes/authRoutes.js');
const authAutenRouter = require('./routes/authAutenTokenRoutes.js');
const reservaRouter = require('./routes/reservasRoutes.js');
const imagenRouter = require('./routes/imagenRoutes.js');
const habitacionesRouter = require('./routes/habitacionesRouter.js');
const habitacion = require('./routes/habitacionRoutes.js');
const userRouter = require('./routes/usuarioRouter.js');
const compression = require('compression');
const zlib= require("zlib");
require('./config/database.js');
require('dotenv').config();


const PORT = process.env.PORT || 3000;



const app = express();
app.use(compression());


//Manejamiento de json
app.use(bodyParser.json({ limit: '50mb' })); 
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true }));

app.use((req, res, next) => {
    if (req.headers['content-encoding'] === 'gzip') {
        let buffer = [];
        req.on('data', (chunk) => buffer.push(chunk));
        req.on('end', () => {
            const decompressed = zlib.gunzipSync(Buffer.concat(buffer)).toString();
            req.body = JSON.parse(decompressed);
            next();
        });
    } else {
        next();
    }
});

//Login
app.use('/api/auth', authRouter);
//Register
app.use('/api/auth', authAutenRouter);

//Reservas
app.use('/api/reservas', reservaRouter);
app.use('/api/habitacion', habitacion);
app.use('/api/user',userRouter);
//Habitacion
app.use('/api/habitaciones', habitacionesRouter);
//Imagen
app.use('/api/imagen', imagenRouter);

//app.use('/user', router);
app.listen(PORT, () => {
    console.log(`Server Started at ${PORT}`);
})