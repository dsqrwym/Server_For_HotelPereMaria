const { Router } = require('express');
const habitacionController = require('../controllers/habitacionController.js');
const { authenticate, authorize } = require('../middleware/auth.js');

const router = Router();

router.use(authenticate);

router.get('/extras', habitacionController.getRoomExtras);
router.get('/types', habitacionController.getRoomTypes)

module.exports = router;
