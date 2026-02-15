const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const mongoose = require('mongoose');
const User = mongoose.model('users');
const IotDevice = mongoose.model('iot');

passport.use(
    new LocalStrategy(
        {usernameField: 'email'},
        async (username, password, done) => {
            const query = await User.findOne({email: username}).exec();

            // No username error trap
            if (!query){
                return done(
                    null,
                    false,
                    {message: 'Incorrect Credentials'}
                );
            }

            // Invalid password trap
            if (!query.validPassword(password)){
                return done(
                    null,
                    false,
                    {message: 'Incorrect Credentials'}
                );
            }

            // Good username and password
            return done(null, query);
        }
    )
);

passport.use(
    'iot',
    new LocalStrategy(
        {usernameField: 'deviceId', passwordField: 'secret'},
        async (deviceId, secret, done) => {
            try {
                const device = await IotDevice.findById(deviceId).exec();
                if (!device) return done(null, false, {message: 'Device not found'});
                if (!device.validSecret(secret)) {
                    return done(null, false, {message: 'Incorrect Credentials'});
                }

                return done(null, device);
            } catch (err) {
                return done(err);
            }
        }
    )
)

module.exports = passport;