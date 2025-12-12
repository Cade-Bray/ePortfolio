const mongoose = require('mongoose');

// Define the device schema
const iotDeviceSchema = new mongoose.Schema({
    id: {type: String, required: true, unique: true},
    name: {type: String, required: true},
    state: {type: String, required: true},
    hash: {type: String, required: true},
    salt: {type: String, required: true},
    ownership: {type: Array}
});

const iotDevice = mongoose.model('iot', iotDeviceSchema);
module.exports = iotDevice;