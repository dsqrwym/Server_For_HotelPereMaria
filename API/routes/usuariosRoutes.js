const express = require('express');
const router = express.Router();
const Usuario = require('../models/usuarios');
const { authenticate } = require('../middleware/auth.js');
const mongoose = require("mongoose");
router.use(authenticate);

router.get('/', async (req, res) => {
    try {
        const { nombre, apellido, email, NIF, username, tipo } = req.query;
        let query = { desactivado: false };

        if (nombre) query['nombre'] = { $regex: new RegExp(nombre, 'i') };
        if (apellido) query['apellido'] = { $regex: new RegExp(apellido, 'i') };
        if (email) query['email'] = { $regex: new RegExp(email, 'i') };
        if (NIF) query['NIF'] = { $regex: new RegExp(NIF, 'i') };
        if (username) query['username'] = { $regex: new RegExp(username, 'i') };
        if (tipo) query['tipo'] = parseInt(tipo);

        const usuarios = await Usuario.find(query).populate('id_avatar');
        res.status(200).json(usuarios);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al obtener usuarios" });
    }
});


router.get('/:username', async (req, res) => {
    try {
        const usuario = await Usuario.findById(req.params.id).populate('id_avatar');
        if (!usuario || usuario.desactivado) {
            return res.status(404).json({ error: 'Usuario no encontrado o desactivado' });
        }
        res.status(200).json(usuario);
    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Error al obtener el usuario" });
    }
});

router.put("/:id", async (req, res) => {
    try {
        const id = req.params.id;

        console.log("actualizacion ID:", id);
        console.log("datos:", req.body);

        if (!mongoose.Types.ObjectId.isValid(id)) {
            console.log("ID no válido");
            return res.status(400).json({ error: "ID no válido" });
        }

        const usuarioAntes = await Usuario.findById(id);
        if (!usuarioAntes) {
            console.log("Usuario no encontrado");
            return res.status(404).json({ error: "Usuario no encontrado" });
        }

        console.log("Usuario antes de la actualizacion:", usuarioAntes);

        const usuarioDespues = await Usuario.findByIdAndUpdate(id, req.body, { new: true });

        console.log("Usuario despues de la actualizacion:", usuarioDespues);

        res.json({
            mensaje: "Usuario actualizado correctamente",
            antes: usuarioAntes,
            despues: usuarioDespues
        });

    } catch (error) {
        console.error("Error en la actualizacion:", error);
        res.status(500).json({ error: error.message });
    }
});

router.post("/new", async (req, res) => {
    try {
        const { nombre, apellido, telefono, email, NIF, username, password, tipo, id_avatar } = req.body;

        if (!email || !username || !password || tipo === undefined) {
            return res.status(400).json({ error: "Faltan campos obligatorios" });
        }

        const tiposPermitidos = [1, 2, 3, 5];
        if (!tiposPermitidos.includes(tipo)) {
            return res.status(400).json({ error: "Tipo de usuario no válido" });
        }

        const nuevoUsuario = new Usuario({
            nombre,
            apellido,
            telefono,
            email,
            NIF,
            username,
            password,
            tipo,
            id_avatar: id_avatar || null 
        });

        await nuevoUsuario.save();

        res.status(201).json({ mensaje: "Usuario creado exitosamente", usuario: nuevoUsuario });

    } catch (error) {
        console.error("Error al crear usuario:", error);
        res.status(500).json({ error: "Error interno del servidor" });
    }
});

module.exports = router;
