const express = require('express');
const cors = require('cors');
require('dotenv').config();
require('./models/db'); // Connection to database.

const app = express();

// Middleware
app.use(cors());
app.use(express.json());

// 404 handler
app.use((req, res) => {
    res.status(404).json({error: 'Not Found'});
});

// Error handler
app.use((err, req, res, next) => {
   console.error(err);
   res.status(err.status || 500).json({error: err.message || 'Internal Server Error'});
});

// Start server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () =>{
    console.log(`Server listening on port ${PORT}`);
});