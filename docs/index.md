# Introduction
Welcome to my ePortfolio and let me introduce you to the project at hand. I've built a IoT thermostat with a rasperry pi 4 and a GPIO breadboard. The device itself has a I2C temperature and humidity sensor. The device takes user input through three buttons; the first incriments the temperature, the second cycles the mode from `OFF→ HEAT→ COOL→ OFF`, and finally the last button decrements the temperature. As an output to the user we have two methods. The first commincates the mode of the device, temperature, and time on a 16x2 grid display. The second output method communicates to a Node JS backend that manages a mongoDB to synchronize its session. That Node JS backend also enacts various forms of business logic for the API endpoints. These endpoints are also utilized by an Angular SPA front end. The SPA is used as a second medium of control for the IoT device where a user can view and adjust temperature and mode on as many IoT devices as registered. The SPA utilizes a bootstrap enabled minimal design to enhance the user experience. 

[![Watch the demo](https://img.youtube.com/vi/PNeJJ2FwW3w/hqdefault.jpg)](https://www.youtube.com/watch?v=PNeJJ2FwW3w)

# Deployment Instructions

### Setting up the Node JS environment
1. Ensure that you have git installed with `git -v` if you don't you can install it [HERE](https://git-scm.com/install/).
2. Ensure that you have mongodb installed with `mongod --version`. If you don't have it installed can install it [HERE](https://www.mongodb.com/try/download/community).
3. Clone this repository with `git clone https://github.com/Cade-Bray/ePortfolio.git`.
4. Create a .env file in the Node Backend file that contains your JWT secret as `JWT_SECRET=yoursecrethere` for example. This will be loaded on runtime.
5. This step various depending on your operating system. You need to open port 3000 for the node backend to communicate. On a debian based operating system you can add a firewall rule with `sudo ufw allow 3000`.
6. Navigate to the back end with `cd ePortfolio/Node_Backend`.
7. Install the dependencies from the packages.json with `npm install`.
8. Launch the application with `npm start`.

### Setting up your device
1. The first step to setting up the raspberry pi 4 device itself is wiring the GPIO. Visit [THIS](gpio) page to wire up your device accordingly.
2. Next is to load the operating system to your thermostat device. Any linux based operating system can be uitlized and I've chosen Ubuntu server 24.04.4 LTS.
3. Generate a device ID and secret using the production script in this repository. Run `source Production_Tool.sh` and follow the terminal instructions. You can batch generate device ID's and secrets similar to a production environment.
4. Load your device secret you recieved from the production script into the /etc/environment as `DEVICE_SECRET=devicesecrethere` for example.
5. Load your device ID you recieved from the production script into the /etc/environment as `DEVICE_ID=deviceidhere` for example.
6. Load your Node JS backend address in the /etc/environment as `ENDPOINT=backendpointhere` for example.
7. Install java on the device by following the Ubuntu 'Install the Java Runtime Environment' guide found [HERE](https://ubuntu.com/tutorials/install-jre#1-overview).
8. Use the following command to download the .jar file from this github repository's releases to the device.
```Bash
REPO="Cade-Bray/ePortfolio"
JAR_NAME=$(curl -sL "https://api.github.com/repos/$REPO/releases/latest" \
          | jq -r '.assets[] | select(.name | endswith(".jar")) | .name') && \
URL=$(curl -sL "https://api.github.com/repos/$REPO/releases/latest" \
      | jq -r ".assets[] | select(.name == \"$JAR_NAME\") | .browser_download_url") && \
wget -q "$URL" -O "$JAR_NAME"
```
6. Launch the .jar file with `java -jar "$JAR_NAME"` in the same terminal as before and if the device is configured correctly you will see regular state output appear on the display.

### Setting up the Angular environment
1. Using the same cloned repository navigate to the ePortfolio/SPA_Frontend. If you're in the same terminal session still you can use `cd ../SPA_Frontend`.
2. Install the dependencies from the packages.json with `npm install`.
3. Create a .env file in the SPA Frontend folder that contains your Node JS endpoint such as `NODE_ENDPOINT=http://localhost:3000/api` for example. Include the '/api' exactly at the end of your address and port number.
4. Launch the application with `npm start`.

**CONGRATULATIONS!** You've now configured and deployed the thermostat IoT project. Navigate to `http://localhost:4200/` to view the angular page. Register for an account and add the device ID you configured earlier in the device /etc/environments file.
