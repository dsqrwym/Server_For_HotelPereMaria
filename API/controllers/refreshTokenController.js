const User = require('../models/usuarios.js');
const jwt = require('jsonwebtoken');

exports.refreshToken = async (req, res) => {
    const { refreshToken } = req.body;

    if (!refreshToken) return res.status(401).json({ error: 'Refresh Token is required' });

    try {
        const decoded = jwt.verify(refreshToken, process.env.JWT_SECRET);

        const user = await User.findById(decoded.id);
        if (!user) return res.status(404).json({ error: 'User not found' });

        if (user.refreshToken !== refreshToken) {
            return res.status(403).json({ error: 'Invalid Refresh Token' });
        }

        const newAccessToken = jwt.sign({ id: user._id, tipo: user.tipo }, process.env.JWT_SECRET, {
            expiresIn: "30m",
        });

        res.json({ accessToken: newAccessToken });
    } catch (error) {
        res.status(400).json({ error: 'Invalid Refresh Token' , detalles: error.message});
    }
};
