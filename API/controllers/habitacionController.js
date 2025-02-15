const Habitacion = require('../models/habitacion.js');
const { handleValidationError, sendResponse } = require('../utils/util.js');

exports.getRoomTypes = async (req, res) => {
    try {
        const roomTypes = await Habitacion.aggregate([
            { 
                $group: { 
                    _id: "$Tipo",  // 按房间类型去重
                    capacidadMax: { $max: "$Capacidad" }  // 取该类型房间的最大容量
                } 
            },
            { 
                $project: { 
                    _id: 0, 
                    tipo: "$_id", 
                    capacidad: "$capacidadMax" 
                } 
            }
        ]);

        if (roomTypes.length === 0) {
            return sendResponse(res, 404, 'No se encontraron tipos de habitación disponibles');
        }

        sendResponse(res, 200, 'Tipos de habitación obtenidos con éxito', roomTypes);
    } catch (error) {
        handleValidationError(res, error, 'Error al obtener los tipos de habitación');
    }
};

// 获取房间表中的所有房间类型并去重
exports.getRoomExtras = async (req, res) => {
    try {
        const extras = await Habitacion.aggregate([
            { 
                $project: { 
                    extrasArray: { $objectToArray: "$Extras" } // 将 Map 转换为数组
                } 
            },
            { $unwind: "$extrasArray" }, // 拆分数组
            { 
                $group: { 
                    _id: "$extrasArray.k",  // 以 extra 的 key（nombre）去重
                    precio: { $first: "$extrasArray.v" } // 取对应的价格
                } 
            },
            { 
                $project: { 
                    _id: 0, 
                    nombre: "$_id", 
                    precio: 1 
                } 
            }
        ]);

        if (extras.length === 0) {
            return sendResponse(res, 404, 'No se encontraron servicios adicionales disponibles');
        }

        sendResponse(res, 200, 'Servicios adicionales obtenidos con éxito', extras);
    } catch (error) {
        console.error(error); // 打印详细错误信息，方便调试
        handleValidationError(res, error, 'Error al obtener los servicios adicionales');
    }
};
