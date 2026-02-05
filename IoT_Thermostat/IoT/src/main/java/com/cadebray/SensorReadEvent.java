package com.cadebray;

/**
 * This record is designed with future functionality in mind. We're ignoring unused because ultimately there is minimal
 * risk to this object going unused. Additionally, this record can hold more values as needed in the future.
 * @param reading
 */
@SuppressWarnings("unused")
public record SensorReadEvent(double[] reading) {

    /**
     * Get the humidity reading from the event
     * @return double value representing the percentage of humidity.
     */
    public double getHumidity() {
        return reading[0];
    }

    /**
     * Get the Fahrenheit value from the most recent reading.
     * @return double value representing the Fahrenheit reading
     */
    public double getFahrenheit() {
        return reading[1];
    }

    /**
     * Get the Celsius value from the most recent reading.
     * @return double value representing the Celsius reading
     */
    public double getCelsius() {
        return reading[2];
    }
}
