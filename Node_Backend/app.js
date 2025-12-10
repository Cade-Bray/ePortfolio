require('dotenv').config();
if (!process.env.JWT_SECRET) {
    console.error('FATAL: process.env.JWT_SECRET is not set');
    process.exit(1); // fail fast so the app doesn't run without a secret
}

const express = require('express');
const passport = require('passport');
const cors = require('cors');
require('./models/db'); // Connection to database.
require('./config/passport'); // Passport configuration

const app = express();

// Middleware
app.use(cors());
app.use(express.json());
app.use(passport.initialize());
app.use(express.urlencoded({extended: true}));

// Routes
const apiRouter = require('./routes/index');
app.use('/api', apiRouter);

// 404 handler
app.use((req, res) => {
    res.status(404).json({error: 'Not Found'});
});

// Error handler
app.use((err, req, res) => {
   console.error(err);
   res.status(err.status || 500).json({error: err.message || 'Internal Server Error'});
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () =>{
    console.log(`Server listening on port ${PORT}`);
});

module.exports = app;