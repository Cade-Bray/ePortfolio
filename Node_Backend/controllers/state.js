const mongoose = require('mongoose');
const Model = mongoose.model('iot');

/**
 * GET - /iot <br>
 * This function lists all the iot devices.
 * @param req Express provided requirements
 * @param res Express provided response.
 * @return {Promise<*>} Returns a packed express response with status code 200/404 with JSON content.
 */
async function iotList(req, res) {
    const query = await Model
        .find({"auth_users": { $in: [new mongoose.Types.ObjectId(req.auth._id)]}})
        .exec();

    // Remove salt, hash, and mongoose schema version from the returned documents for security reasons
    const sanitizedQuery = query.map(device => {
        const deviceObj = device.toObject();
        delete deviceObj.salt;
        delete deviceObj.hash;
        delete deviceObj.__v;
        return deviceObj;
    });

    if (!sanitizedQuery) {
        // Database returned nothing in this instance
        return res.status(404).json({message: 'IoT Devices couldn\'t be found!'});
    } else {
        // Good query, 200 and pack query return.
        return res.status(200).json(sanitizedQuery);
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
        .findOne({
                '_id': req.params.iotCode,
                // Access control to ensure that only the device itself or an authorized user can update the device info.
                $or: [
                    {'auth_users': { $in: [new mongoose.Types.ObjectId(req.auth._id)]}},
                    {'_id': req.auth._id}
                ]
        }).exec();

    if (!query) {
        return res.status(404).json({message: 'IoT couldn\'t be found or unauthorized to access.'});
    }

    // Sanitize the returned document for security reasons.
    const deviceObj = query.toObject();
    delete deviceObj.salt;
    delete deviceObj.hash;
    delete deviceObj.__v;

    return res.status(200).json(deviceObj);
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
        {
            '_id': req.params.iotCode,
            // Access control to ensure that only the device itself or an authorized user can update the device info.
            $or: [
                {'auth_users': { $in: [new mongoose.Types.ObjectId(req.auth._id)]}},
                {'_id': req.auth._id}
            ]
        },
        {
            name: req.body.name,
            state: req.body.state,
            setTemp: req.body.setTemp,
            // Update the lastChecked and currentTemp only if it's the device itself not an authorized user.
            // This is to prevent users from updating the lastChecked field when they update the device information.
            // Using the spread operator to conditionally add the lastChecked field.
            ...(req.params.iotCode === req.auth._id && { lastChecked: new Date() }),
            ...(req.params.iotCode === req.auth._id && { currentTemp: req.body.currentTemp })
        }
    ).exec();

    if (!query){
        // Database returned nothing
        return res.status(400).json({message: 'Update failed or unauthorized'});
    } else {
        // Return the resulting updated document
        return res.status(201).json({message: `IoT device with code ${req.params.iotCode} has been updated.`});
    }
}

/**
 * DELETE - /iot/:iotCode
 * This function will delete the given iot code found in the database.
 * @param req Express provided requirements. This is used to grab the iot code from the parameters.
 * @param res Express provided requirements. This is used for the packed response.
 */
async function iotsDeleteIot(req, res) {

    const query = await Model.findOneAndDelete(
        {
            '_id': req.params.iotCode,
            'auth_users': { $in: [new mongoose.Types.ObjectId(req.auth._id)]}
        }
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
    iotsUpdateIot,
    iotsDeleteIot
}