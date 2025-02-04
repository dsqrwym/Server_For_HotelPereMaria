const jwt = require('jsonwebtoken');
const User = require('../models/usuarios.js');

exports.authenticate = async (req, res, next) => {
    const token = req.header('Authorization')?.split(' ')[1]; // Bearer token --> Authorization: Bearer abc123xyz 
    if (!token) return res.status(401).json({ error: 'Access denied' });

    try {
        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        req.user = await User.findById(decoded.id);
        if (!req.user) return res.status(404).json({ error: 'User not found' });
        next();
    } catch (error) {
        res.status(400).json({ error: 'Invalid token' });
    }
};

exports.authorize = (roles) => (req, res, next) => {
    const rolesString = {
        1: 'client',
        2: 'employee',
        3: 'admin',
        5: 'superadmin'
    };
    const role = req.user.tipo;
    if (!roles.includes(role)) {
        console.log(`Unauthorized access attempt by user with role ${role} --> ${rolesString[role]}`);
        return res.status(403).json({ error: 'Permission denied' });
    }
    next();
};


/*
Ejemplo de uso, si no es tipo 3 no puede actualizar un usuario 
const { authenticate, authorize } = require("../middleware/auth");

router.put("/:id", authenticate, authorize([3]), updateUser);
*/
