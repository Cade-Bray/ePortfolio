package com.cadebray;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectFactory;

/**
 * Service to manage the LCD updates based on the thermostat state and temperature readings.
 */
@Service
public class DisplayService {
    private final ObjectFactory<StateMachine<States, Events>> stateMachineFactory;
    private final LCDisplay lcd;
    private final AHT20 aht20;
    private final ThermostatProperties thermostatProperties;
    private int counter = 0;

    /**
     * Constructor for DisplayService. This service updates the LCD based on the current state and temperature readings.
     * @param stateMachineFactory This is the factory to create StateMachine instances
     * @param lcd This is the LCDisplay instance for the LCD
     * @param aht20 This is the AHT20 temperature and humidity sensor instance
     */
    public DisplayService(ObjectFactory<StateMachine<States, Events>> stateMachineFactory,
                          LCDisplay lcd,
                          AHT20 aht20, ThermostatProperties thermostatProperties) {
        this.stateMachineFactory = stateMachineFactory;
        this.lcd = lcd;
        this.aht20 = aht20;
        this.thermostatProperties = thermostatProperties;
    }

    private StateMachine<States, Events> getStateMachine() {
        return stateMachineFactory.getObject();
    }

    /**
     * Scheduled method to update the LCD every second.
     */
    @Scheduled(fixedDelay = 1000)
    public void tick(){
        StateMachine<States, Events> stateMachine = getStateMachine();
        if (stateMachine.getState() == null) {
            // State machine not initialized yet
            return;
        }

        double temperature;
        try{
            temperature = aht20.readSensor()[1];
        } catch (Exception e){
            lcd.clear();
            lcd.setCursor(0, 0);
            lcd.print("Sensor Error");
            return;
        }

        States currentState = stateMachine.getState().getId();

        double setpoint = Double.NaN;
        try {
            setpoint = thermostatProperties.getSetpoint();
        } catch (Exception e) {
            // Ignore, will show as NaN
        }

        // tick and alternate display every 10 seconds
        counter = (counter + 1) & Integer.MAX_VALUE;
        boolean showTemperature = ((counter / 10) % 2) == 0;

        lcd.clear();
        lcd.setCursor(0, 0);

        String timeStr = java.time.LocalTime.now().withNano(0).toString();
        String stateStr = switch (currentState) {
            case OFF -> "OFF";
            case COOL -> "COOL";
            case HEAT -> "HEAT";
        };
        String line1 = String.format("%-8s %s", timeStr, stateStr);

        String line2;
        if (showTemperature) {
            line2 = String.format("Temp: %.1fF", temperature);
        } else {
            line2 = !Double.isNaN(setpoint) ? String.format("Set Temp:  %.1fF", setpoint) : "Set:  --F";
        }

        // Print both lines to the LCD
        lcd.print(line1 + "\n" + line2);
    }
}
