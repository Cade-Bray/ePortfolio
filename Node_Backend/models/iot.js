const mongoose = require('mongoose');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');

// Define the device schema
const iotDeviceSchema = new mongoose.Schema({
    name: {type: String, required: true},
    state: {type: String},
    currentTemp: {type: Number},
    setTemp: {type: Number},
    lastChecked: {type: Date, default: Date.now()},
    hash: {type: String},
    salt: {type: String},
    auth_users: {type: Array}
});

// Method to set the secret on this record.
iotDeviceSchema.methods.setSecret = function(secret){
    this.salt = crypto.randomBytes(16).toString();
    this.hash = crypto.pbkdf2Sync(
        secret,
        this.salt,
        1000,
        64,
        'sha512'
    ).toString();
};

// Method to validate the secret on this record against the hash
iotDeviceSchema.methods.validSecret = function (secret) {
    const hash = crypto.pbkdf2Sync(
        secret,
        this.salt,
        1000,
        64,
        'sha512'
    ).toString();
    return this.hash === hash;
};

// Method to generate the JWT
iotDeviceSchema.methods.generateJWT = function () {
    return jwt.sign(
        { // Payload for our JSON Web Token
            _id: this._id
        },
        process.env.JWT_SECRET, // Secret stored in the .env file
        {expiresIn: '1m'} // Short-lived token for microtransactions.
    );
};

const iotDevice = mongoose.model('iot', iotDeviceSchema, 'iots');
module.exports = iotDevice;