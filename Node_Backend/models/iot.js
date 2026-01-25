const mongoose = require('mongoose');

// Define the device schema
const iotDeviceSchema = new mongoose.Schema({
    name: {type: String, required: true},
    state: {type: String, required: true},
    currentTemp: {type: Number},
    setTemp: {type: Number, required: true},
    lastChecked: {type: Date, required: true},
    hash: {type: String, required: true},
    salt: {type: String, required: true},
    auth_users: {type: Array}
});

const iotDevice = mongoose.model('iot', iotDeviceSchema);
module.exports = iotDevice;