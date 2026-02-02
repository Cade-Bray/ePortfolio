package com.cadebray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This class holds the thermostat properties, specifically the temperature setpoint.
 * It uses AtomicReference to ensure thread-safe operations when modifying the setpoint.
 * The setpoint can be configured via application properties with the prefix "thermostat".
 * This needs to be a component so it can be injected where needed.
 */
@Component
@ConfigurationProperties(prefix = "thermostat")
public class ThermostatProperties {
    private final AtomicReference<Double> setpoint = new AtomicReference<>(72.0);
    private final String objectId = "thermostat-001"; // TODO make configurable

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * Set the temperature setpoint.
     * @param setpoint The desired temperature setpoint
     */
    public synchronized void setSetpoint(Double setpoint) {
        this.setpoint.set(setpoint);
        if (publisher != null) {
            publisher.publishEvent(setpoint);
        }
    }

    /**
     * Get the current temperature setpoint.
     * @return The current temperature setpoint
     */
    public double getSetpoint() {
        return setpoint.get();
    }

    /**
     * Increment the temperature setpoint by 0.5 degree.
     * @return The new temperature setpoint
     */
    public double incrementSetpoint() {
        double newVal = setpoint.updateAndGet(v -> v + 0.5);
        if (publisher != null) publisher.publishEvent(newVal);
        return newVal;
    }

    /**
     * Decrement the temperature setpoint by 0.5 degree.
     * @return The new temperature setpoint
     */
    public double decrementSetpoint() {
        double newVal = setpoint.updateAndGet(v -> v - 0.5);
        if (publisher != null) publisher.publishEvent(newVal);
        return newVal;
    }
}
