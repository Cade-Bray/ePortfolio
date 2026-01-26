const mongoose = require('mongoose');

// Define the device schema
const iotDeviceSchema = new mongoose.Schema({
    name: {type: String, required: true},
    state: {type: String, required: true},
    currentTemp: {type: Number},
    setTemp: {type: Number, required: true},
    lastChecked: {type: Date, required: true},
    hash: {type: String, required: false},
    salt: {type: String, required: false},
    auth_users: {type: Array, required: true}
});

const iotDevice = mongoose.model('iot', iotDeviceSchema, 'iot');
module.exports = iotDevice;