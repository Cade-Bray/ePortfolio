# Introduction
Welcome to my ePortfolio and let me introduce you to the project at hand. I've built a IoT thermostat with a rasperry pi 4 and a GPIO breadboard. The device itself has a I2C temperature and humidity sensor. The device takes user input through three buttons; the first incriments the temperature, the second cycles the mode from OFF-> HEAT-> COOL-> OFF, and finally the last button decrements the temperature. As an output to the user we have two methods. The first commincates the mode of the device, temperature, and time on a 16x2 grid display. The second output method communicates to a Node JS backend that manages a mongoDB to synchronize its session. That Node JS backend also enacts various forms of business logic for the API endpoints. These endpoints are also utilized by an Angular SPA front end. The SPA is used as a second medium of control for the IoT device where a user can view and adjust temperature and mode on as many IoT devices as registered. The SPA utilizes a bootstrap enabled minimal design to enhance the user experience. 

[![Watch the demo](https://img.youtube.com/vi/PNeJJ2FwW3w/hqdefault.jpg)](https://www.youtube.com/watch?v=PNeJJ2FwW3w)

# Deployment Instructions

### Setting up your device
1. The first step to setting up the device itself is wiring the GPIO. Visit [THIS](gpio.md) page to wire up your device accordingly.
2. Next is to load the operating system to your thermostat device. Any linux based operating system can be uitlized and I've chosen Ubuntu server 24.04.4 LTS.
3. Load your device secret into the /etc/environment as `DEVICE_SECRET=SuperSecret` for example.
4. Install java on the device by following the Ubuntu 'Install the Java Runtime Environment' guide found [HERE](https://ubuntu.com/tutorials/install-jre#1-overview).
5. Use the following command to download the .jar file from this github repository's releases to the device.
```Bash
TODO
```
6. Launch the .jar file with `TODO` and if the device is configured correctly you will see regular state output appear on the display.

### Setting up the Node JS environment
1. TODO

### Setting up the Angular environment
1. TODO
