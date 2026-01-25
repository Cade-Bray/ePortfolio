const express = require('express');
const router = express.Router();
const stateCtrl = require('../controllers/state');
const authCtrl = require('../controllers/authentication');
const jwt = require('jsonwebtoken'); // Enable JWT

/**
 * Middleware function to authenticate JWT tokens.
 * @param req This is the request object
 * @param res This is the response object
 * @param next This is the next function in the middleware chain
 * @return {*} Returns a 401 Unauthorized if the token is invalid or missing, otherwise calls next()
 */
function authenticateJWT(req, res, next) {
    // Error trap for catching a missing auth header
    const authHeader = req.headers['authorization'];
    if (authHeader === null || authHeader === undefined) {
        return res.sendStatus(401);
    }

    // Error trap for a header that has too few tokens
    const headers = authHeader.split(' ');
    if (headers.length < 1) {
        return res.sendStatus(501);
    }

    // Error trap for no tokens
    const token = authHeader.split(' ')[1];
    if (token === null) {
        return res.sendStatus(401);
    }

    // Verifying the jwt
    jwt.verify(token, process.env.JWT_SECRET, (err, verified) => {
        if (err){
            return res.sendStatus(401).json({message: 'Token validation error!'});
        }

        // Pack the decoded information into the auth header to travel further down the line.
        req.auth = verified;
        next();
    });
}

// This is the registration endpoint and login endpoint.
router.post('/register', authCtrl.register);
router.post('/login', authCtrl.login);

// IoT device endpoints
router
    .route('/iot')
    .get(authenticateJWT, stateCtrl.iotList) // GET request for all IoT devices
    .post(authenticateJWT, stateCtrl.iotsAddIot); // POST request to create an IoT device

// IoT device specific endpoints
router
    .route('/iot/:iotCode')
    .get(stateCtrl.iotsFindByCode)
    .put(authenticateJWT, stateCtrl.iotsUpdateIot)
    .delete(authenticateJWT, stateCtrl.iotsDeleteIot);

module.exports = router;