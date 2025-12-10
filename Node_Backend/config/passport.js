const passport = require('passport');
const LocalStrategy = require('passport-local').Strategy;
const mongoose = require('mongoose');
const User = mongoose.model('users');

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

module.exports = passport;