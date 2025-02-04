const mongoose = require('mongoose');
const dotenv = require('dotenv');

dotenv.config();

const mongoString = process.env.DATABASE_URL

//Conexion a Mongo
mongoose.connect(mongoString).then(() => {
    console.log(`Database Connected: ${mongoose.connection.name}`);
}).catch((error) => {
    console.error('Error:', error);
});

module.exports = mongoose;