const { Router } = require('express');
const imagenController = require('../controllers/imagenController.js');
const { authenticate, authorize } = require('../middleware/auth.js');

const router = Router();

router.use(authenticate);

router.post('/user', imagenController.subirUserImagen);

router.post('/habitacion', authorize([2, 3, 5]), imagenController.subirHabitacionImagen);

router.post('/default/user', authorize([3, 5]), imagenController.subirDefautImagen);

router.post('/default/habitacion', authorize([3, 5]), imagenController.subirDefaultHab);

router.get('/habitacion', imagenController.obtenerFotosHabitacion);

router.get('/habitacion/:id', imagenController.getHabImagen);

router.get('/user/:id', imagenController.getUserImagen);

router.delete('/user/:id', imagenController.deleteImagenById);

router.delete('/habitacion/:id', authorize([2, 3, 5]), imagenController.deleteHabImagenById);

router.delete('/default/:id', authorize([3, 5]), imagenController.deleteDefaultImagenById);

module.exports = router;
