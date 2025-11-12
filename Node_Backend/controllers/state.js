const mongoose = require('mongoose');
const Model = mongoose.model('iot');

/**
 * GET - /iot <br>
 * This function lists all the iot devices.
 * @param req Express provided requirements
 * @param res Express provided response.
 * @return {Promise<*>} Returns a packed express response with status code 200/404 with json content.
 */
async function iotList(req, res) {
    const query = await Model
        .find({})
        .exec();

    if (!query) {
        // Database returned nothing in this instance
        return res.status(404).json({message: 'IoT Devices couldn\'t be found!'});
    } else {
        // Good query, 200 and pack query return.
        return res.status(200).json(query);
    }
}

module.exports = {
    iotList
}