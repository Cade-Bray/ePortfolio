package com.cadebray;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
public class TempMonitor {
    private final AHT20 aht20;
    private final ThermostatProperties thermostatProperties;
    private final LedService ledService;
    private Integer lastRelation = null;

    public TempMonitor(AHT20 aht20, ThermostatProperties thermostatProperties, LedService ledService) {
        this.aht20 = aht20;
        this.thermostatProperties = thermostatProperties;
        this.ledService = ledService;
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        double temp;
        try {
            temp = aht20.readSensor()[1];
        } catch (Exception e) {
            // Sensor read error, skip this cycle
            return;
        }

        double setpoint = thermostatProperties.getSetpoint();
        int relation = relationFor(temp, setpoint);

        if (lastRelation == null) {
            // Initialize baseline
            lastRelation = relation;
            return;
        }

        if (!Objects.equals(lastRelation, relation)) {
            // crossing detected - update LEDs
            lastRelation = relation;
        }
    }

    private int relationFor(double temp, double setpoint) {
        if (Double.isNaN(temp) || Double.isNaN(setpoint)) {
            return 0; // unknown
        } else if (temp < setpoint) {
            return -1; // below
        } else if (temp > setpoint) {
            return 1; // above
        } else {
            return 0; // equal
        }
    }

    @EventListener
    public void onSetpointChanged(Double newSetpoint) {
        double temp;
        try {
            temp = aht20.readSensor()[1];
        } catch (Exception e) {
            lastRelation = null;
            return;
        }
        lastRelation = relationFor(temp, newSetpoint);
    }
}
