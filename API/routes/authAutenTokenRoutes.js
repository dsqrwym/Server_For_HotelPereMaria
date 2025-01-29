const { Router } = require('express');
const authController = require('../controllers/authController.js');
const { authorize, authenticate } = require('../middleware/auth.js');

const router = Router();
router.use(authenticate);

router.post('/register/newClient', authorize([2, 3, 5]), authController.registerClient);
router.post('/register/newEmployee', authorize([3, 5]), authController.registerEmployee);
router.post('/register/newAdmin', authorize([5]) , authController.registerAdmin);

module.exports = router;