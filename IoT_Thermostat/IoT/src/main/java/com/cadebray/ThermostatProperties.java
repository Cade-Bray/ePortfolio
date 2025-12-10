package com.cadebray;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class holds the thermostat properties, specifically the temperature setpoint.
 * It uses AtomicInteger to ensure thread-safe operations when modifying the setpoint.
 * The setpoint can be configured via application properties with the prefix "thermostat".
 * This needs to be a component so it can be injected where needed.
 */
@Component
@ConfigurationProperties(prefix = "thermostat")
public class ThermostatProperties {
    private final AtomicInteger setpoint = new AtomicInteger(72);

    /**
     * Set the temperature setpoint.
     * @param setpoint The desired temperature setpoint
     */
    public void setSetpoint(int setpoint) {
        this.setpoint.set(setpoint);
    }

    /**
     * Get the current temperature setpoint.
     * @return The current temperature setpoint
     */
    public int getSetpoint() {
        return setpoint.get();
    }

    /**
     * Increment the temperature setpoint by 1 degree.
     * @return The new temperature setpoint
     */
    public int incrementSetpoint() {
        return setpoint.incrementAndGet();
    }

    /**
     * Decrement the temperature setpoint by 1 degree.
     * @return The new temperature setpoint
     */
    public int decrementSetpoint() {
        return setpoint.decrementAndGet();
    }
}
