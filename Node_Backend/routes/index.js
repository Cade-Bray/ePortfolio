const express = require('express');
const router = express.Router();
const stateCtrl = require('../controllers/state');
const authCtrl = require('../controllers/authentication');
const jwt = require('jsonwebtoken'); // Enable JWT

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

router.route('/register').post(authCtrl.register);
router.route('/login').post(authCtrl.login);

router
    .route('/trips')
    .get(tripsCtrl.tripsList) // GET request for all trips
    .post(authenticateJWT, tripsCtrl.tripsAddTrip); // POST request to create a trip

router
    .route('/trips/:tripCode')
    .get(tripsCtrl.tripsFindByCode)
    .put(authenticateJWT, tripsCtrl.tripsUpdateTrip)
    .delete(authenticateJWT, tripsCtrl.tripsDeleteTrip);

module.exports = router;