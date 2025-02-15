const { Router } = require('express');
const usuarioController = require('../controllers/usuarioConreoller.js');
const { authenticate, authorize } = require('../middleware/auth.js');

const router = Router();

router.use(authenticate);

router.get('/client/', usuarioController.getClientes);
router.get('/employee/', usuarioController.getEmployee);

module.exports = router;