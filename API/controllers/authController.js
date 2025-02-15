const User = require('../models/usuarios.js');
const jwt = require('jsonwebtoken');

exports.login = async (req, res) => {
    const { username, email, password } = req.body;

    try {
        // Buscar usuario
        let user;
        if (email) {
            user = await User.findOne({ email });
        } else if (username) {
            user = await User.findOne({ username });
        }
        if (!user) return res.status(404).json({ error: 'User not found' });

        // Verificar contraseña
        const isPasswordValid = await user.comparePassword(password);
        if (!isPasswordValid) return res.status(401).json({ error: 'Invalid password' });

        // Generar token
        const accessToken = jwt.sign({ id: user._id, tipo: user.tipo }, process.env.JWT_SECRET, {
            expiresIn: '30m',
        });
        const refreshToken = jwt.sign({ id: user._id }, process.env.JWT_SECRET, {
            expiresIn: '3d',
        });

        user.refreshToken = refreshToken;
        await user.save();

        res.json({ accessToken, refreshToken, user });

    } catch (error) {
        console.error('Error during login:', error);
        res.status(500).json({ error: 'Internal server error' });
    }
};


async function createUser(req, res, role){
    const { altUser, nombre, apellido, telefono, email, NIF, username, password, id_avatar } = req.body;

    try {
        const userData = {};
        if (altUser) { userData.altUser = altUser } else { return res.status(400).json({ error: 'altUser is required' }); }
        if (nombre) userData.nombre = nombre;
        if (apellido) userData.apellido = apellido;
        if (telefono) userData.telefono = telefono;
        if (email) userData.email = email;
        if (NIF) userData.NIF = NIF;
        if (username) userData.username = username;
        if (password) userData.password = password;
        if (id_avatar) userData.id_avatar = id_avatar;
        userData.tipo = role;

        const newUser = new User(userData);
        await newUser.save();


        res.status(201).json({
            message: 'User registered successfully',
            user: newUser.toJSON(),
        });

    } catch (error) {
        if (error.code === 11000) {
            return res.status(400).json({
                error: 'Email or username already exists',
            });
        }
        res.status(500).json({ error: `An error occurred during registration${error}` });
    }
}


exports.registerClient = async (req, res) => {
    createUser(req, res, 1);
};

exports.registerEmployee = async (req, res) => {
    createUser(req, res, 2);
};

exports.registerAdmin = async (req, res) => {
    createUser(req, res, 3);
};