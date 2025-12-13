const mongoose = require('mongoose');

// Define the device schema
const iotDeviceSchema = new mongoose.Schema({
    _id: {type: String, required: false, unique: true, index: true},
    name: {type: String, required: true},
    state: {type: String, required: true},
    currentTemp: {type: Number},
    setTemp: {type: Number, required: true},
    lastCheckIn: {type: Date, required: true},
    hash: {type: String, required: true},
    salt: {type: String, required: true},
    ownership: {type: Array}
});

const iotDevice = mongoose.model('iot', iotDeviceSchema);
module.exports = iotDevice;