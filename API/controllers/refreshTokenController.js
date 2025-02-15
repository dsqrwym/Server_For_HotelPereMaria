const User = require('../models/usuarios.js');
const jwt = require('jsonwebtoken');
const { sendResponse } = require('../utils/util.js');

exports.refreshToken = async (req, res) => {
    const { refreshToken } = req.body;

    if (!refreshToken) return sendResponse(res, 401, 'Refresh Token is required');

    try {
        const decoded = jwt.verify(refreshToken, process.env.JWT_SECRET);

        const user = await User.findById(decoded.id);
        if (!user) return sendResponse(res, 404, 'User not found');

        if (user.refreshToken !== refreshToken) {
            return sendResponse(res, 403, 'Invalid Refresh Token');
        }

        const newAccessToken = jwt.sign({ id: user._id, tipo: user.tipo }, process.env.JWT_SECRET, {
            expiresIn: "30m",
        });

        return sendResponse(res, 200, 'Access Token refreshed successfully', { accessToken: newAccessToken });
    } catch (error) {
        return sendResponse(res, 400, 'Invalid Refresh Token', null, error.message);
    }
};
