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

/**
 * GET - /iot/:iotCode <br>
 * This function lists the iot by the given code.
 * @param req Express provided requirements
 * @param res Express provided response.
 * @return {Promise<*>} Returns a packed express response with status code 200/404 with json content.
 */
async function iotsFindByCode(req, res) {
    const query = await Model
        .find({'code': req.params.iotCode})
        .exec();

    if (!query) {
        // Database returned nothing in this instance
        return res.status(404).json({message: 'IoT couldn\'t be found!'});
    } else {
        // Good query, 200 and pack query return.
        return res.status(200).json(query);
    }
}

/**
 * POST - /iot <br>
 * This is the POST response for adding a iot device to the database. Parameters are x-www-form-urlencoded in the body. 
 * <br>
 * @param req Express requirements. This is where the parameters are drawn from to make the request.
 * @param res Express response. Packed HTTP status code and json data.
 * @return {Promise<*>} 201/400. Express response returned with packed HTTP status code and json data.
 */
async function iotsAddIot(req, res){
    const newIot = new Model({
        code: req.body.code,
        name: req.body.name,
        state: req.body.state
    });

    // Save it to the database and await the query response that it's good!
    const query = await newIot.save();

    if (!query){
        // Database returned nothing
        return res.status(400).json({message: 'An error has occurred while posting that data.'});
    } else {
        // Return the new iot device
        return res.status(201).json(query);
    }
}

/**
 * PUT - /iot/:iotCode
 * This function will update an iot object based on the iot code provided in the url parameters.
 * @param req Express Requirements. x-www-form-urlencoded body information used for updating.
 * @param res Express response used to return information.
 * @return {Promise<*>} 201/400. Return is an express response packed with an HTTP status code and JSON formatted data.
 */
async function iotsUpdateIot(req, res) {
    const query = await Model.findOneAndUpdate(
        {'code': req.params.iotCode},
        {
            code: req.body.code,
            name: req.body.name,
            state: req.body.state
        }
    ).exec();

    if (!query){
        // Database returned nothing
        return res.status(400).json(query.error);
    } else {
        // Return the resulting updated document
        return res.status(201).json(query);
    }
}

/**
 * DELETE - /iot/:iotCode
 * This function will delete the given iot code found in the database.
 * @param req Express provided requirements. This is used to grab the iot code from the parameters.
 * @param res Express provided requirements. This is used for the packed response.
 */
function iotsDeleteIot(req, res) {
    const query = Model.findOneAndDelete(
        {'code': req.params.iotCode}
    ).exec();

    if (query === null) {
        return res.status(404).json({message: `There was no iot found under iot code ${req.params.iotCode}`});
    } else if (query) {
        return res.status(200).json(query);
    } else {
        return res.status(400).json({message: 'Bad request'});
    }
}

module.exports = {
    iotList,
    iotsFindByCode,
    iotsAddIot,
    iotsUpdateIot,
    iotsDeleteIot
}