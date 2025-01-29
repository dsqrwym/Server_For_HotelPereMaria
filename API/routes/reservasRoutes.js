const { Router } = require('express');
const reservaController = require('../controllers/reservaController.js');
const { authenticate, authorize } = require('../middleware/auth.js');

const router = Router();

router.use(authenticate);

router.post('/', reservaController.createReserva);

router.get('/', authorize([2, 3, 5]), reservaController.getReservas);

router.get('/:id', reservaController.getReservaById);

router.put('/:id', reservaController.updateReserva);

router.delete('/:id', authorize([2, 3, 5]), reservaController.deleteReserva);

module.exports = router;
