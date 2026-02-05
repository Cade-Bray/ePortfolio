package com.cadebray;

/**
 * This record is designed with future functionality in mind. We're ignoring unused because ultimately there is minimal
 * risk to this object going unused. Additionally, this record can hold more values as needed in the future.
 * @param reading
 */
@SuppressWarnings("unused")
public record SensorReadEvent(double[] reading) {

    public double getHumidity() {
        return reading[0];
    }

    public double getFahrenheit() {
        return reading[1];
    }

    public double getCelsius() {
        return reading[2];
    }
}
