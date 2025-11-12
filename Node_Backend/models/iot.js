const mongoose = require('mongoose');

// Define the device schema
const iotDeviceSchema = new mongoose.Schema({
    code: {type: String, required: true, index: true},
    name: {type: String, required: true, index: true},
    state: {type: String, required: true, index: true}
});

const iotDevice = mongoose.model('iot', iotDeviceSchema);
module.exports = iotDevice;