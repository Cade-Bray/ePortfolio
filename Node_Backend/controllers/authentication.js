const User = require('../models/user');
const IotDevice = require('../models/iot');
const passport = require('passport');
const { verify } = require("jsonwebtoken");

/**
 * Decode JWT token to get user information.
 * @param token JWT token string.
 * @return {Object} Decoded JWT token containing user information.
 */
function decodeToken(token) {
    return verify(token, process.env.JWT_SECRET);
}

/**
 * This is the registration function. The request is parsed to create a new user under the schema. The return is JWT.
 * @param req Express request. This is parsed for the name, email, and password provided in plaintext.
 * @param res Express response. This will be packed with a status code 400/200 and a token if applicable.
 * @return {Promise<*>} Express response with a JSON of a JWT. When the JWT is decoded it contains the user id, email,
 *                      name, iat, and exp.
 */
async function register(req, res) {
    // Validate message to ensure that all parameters are present.
    if (!req.body.name || !req.body.email || !req.body.password) {
        return res.status(400).json({message: 'All fields required'});
    }

    const user = new User({
        name: req.body.name,
        email: req.body.email,
        password: ''
    });

    user.setPassword(req.body.password);
    const query = await user.save();

    if (!query) {
        // Database returned nothing
        return res.status(400).json(err);
    } else {
        // Return a new user token
        const token = user.generateJWT();
        return res.status(200).json({"token": token, "user": user._id});
    }
}

/**
 * This function handles the login authentication.
 * @param req Express provided requirements. Used to parse the email and password.
 * @param res Express provided response. This is packed with a status and JSON data.
 * @param next Express next function in the middleware chain.
 * @return {Promise<void>} Returning a status code 200/400/401/404 packed in an express response. Packed with JSON data.
 */
async function login(req, res, next) {
    // Error trap for not filling out all fields.
    if (!req.body.email || !req.body.password) {
        return res.status(400).json({message: 'All fields are required.'});
    }

    // Using passport for authentication
    passport.authenticate('local', (err, user, info) => {
        // Error trap
        if (err) {
            // Error in authentication process.
            return res.status(404).json(err);
        }
        
        if (user) { // Auth successful
            const token = user.generateJWT();
            return res.status(200).json({"token": token, "user": user._id});
        } else { // Auth failed
            return res.status(401).json(info);
        }
    }) (req, res, next);
}

/**
 * This function handles the login authentication for IoT devices. The tokens 1m short-lived.
 * @param req Express provided requirements. Used to parse the deviceId and secret.
 * @param res Express provided response. This is packed with a status and JSON data.
 * @param next Express next function in the middleware chain.
 * @returns {Promise<*>} Returning a status code 200/400/401/404 packed in an express response. Packed with JSON data.
 */
async function iotLogin(req, res, next) {
    // Error trap for not filling out all fields.
    if (!req.body.deviceId || !req.body.secret) {
        return res.status(400).json({message: 'All fields are required.'});
    }

    // Using passport for authentication
    passport.authenticate('iot', (err, device, info) => {
        // Error trap
        if (err) {
            // Error in authentication process.
            return res.status(404).json(err);
        }

        if (device) { // Auth successful
            const token = device.generateJWT();
            return res.status(200).json({"token": token, "device": device._id});
        } else { // Auth failed
            return res.status(401).json(info);
        }
    }) (req, res, next);
}

/**
 * This is the registration function for IoT devices. The request is parsed to create a new device under the schema.
 * The return is JWT.
 * @param req This is the Express request. This is parsed for the name and secret provided in plaintext.
 * @param res This is the Express response. This will be packed with a status code 400/200 and a token if applicable.
 * @returns {Promise<*>} Express response with a JSON of a JWT.
 */
async function iotRegister(req, res) {
    // Validate message to ensure that all parameters are present.
    if (!req.body.name || !req.body.secret) {
        return res.status(400).json({message: 'All fields required'});
    }

    const device = new IotDevice({
        name: req.body.name,
        secret: ''
    });

    device.setSecret(req.body.secret);
    const query = await device.save();

    if (!query) {
        // Database returned nothing
        return res.status(400).json(err);
    } else {
        // Return a new device token
        const token = device.generateJWT();
        return res.status(200).json({"token": token, "device": device._id});
    }
}

module.exports = { register, login, decodeToken, iotLogin, iotRegister };