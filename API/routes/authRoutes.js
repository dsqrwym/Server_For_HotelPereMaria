const { Router } = require('express');
const authController = require('../controllers/authController.js');
const { refreshToken } = require('../controllers/refreshTokenController.js');

const router = Router();

router.post('/login', authController.login);
router.post('/refresh-token', refreshToken);

module.exports = router;