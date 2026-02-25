# Device Setup Instructions
---

### What you'll need
- 1x Raspberry pi 4
- 1x Canakit RPI4 GPIO ribbon cable
- 1x Canakit GPIO T connector
- 3x Generic GPIO buttons
- 1x Blue LED
- 1x Red LED
- 1x 16x2 Canakit grid display
- 3x 10kΩ resistors
- 2x 220Ω resistors
- 1x Potentiometer
- 24x Dupont cables
- 1x I2C ADAFruit AHT20 Temperature & Humidity Sensor

Before we go any further place your T connector at the top of your GPIO breadboard. Connect the T connector to the RPI GPIO pin set. Finally connect the AHT20 sensor via I2C on the RPI itself.


### Wiring Instructions
---

#### Wiring the display
1. Place the 16x2 Canakit grid display with the left most pin at **D44**. The remainder of the pins will fall into place.
2. Run a Dupont cable from **E44** to any **GND** (Ground) pin.
3. Run a Dupont cable from **E45** to the **POSITIVE 5V** rail.
4. Run a Dupont cable from **E46** to **F46**.
5. Run a Dupont cable from **E47** to **GPIO pin 17**.
6. Run a Dupont cable from **E48** to any **GND** pin.
7. Run a Dupont cable from **E49** to **GPIO pin 27**.
8. Run a Dupont cable from **E54** (which is the D4 pin on the screen) to **GPIO pin 5**.
9. Run a Dupont cable from **E55** to **GPIO pin 6**.
10. Run a Dupont cable from **E56** to **GPIO pin 13**.
11. Run a Dupont cable from **E57** to **GPIO pin 26**.
12. Run a Dupont cable from **E58** to **POSITIVE 5V** rail.
13. Run a Dupont cable from **E59** to **GND** pin.

*Congratulations*, you've setup the display for the thermostat device. Now we'll wire up the potentiometer.


