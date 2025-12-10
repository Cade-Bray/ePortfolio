package com.cadebray;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

/**
 * Service to manage the LCD updates based on the thermostat state and temperature readings.
 */
@Component
public class DisplayService {
    private final StateMachine<States, Events> stateMachine;
    private final LCDisplay lcd;
    private final AHT20 aht20;
    private int counter = 0;

    /**
     * Constructor for DisplayService. This service updates the LCD based on the current state and temperature readings.
     * @param stateMachine This is the state machine managing thermostat states
     * @param lcd This is the LCDisplay instance for the LCD
     * @param aht20 This is the AHT20 temperature and humidity sensor instance
     */
    public DisplayService(StateMachine<States, Events> stateMachine, LCDisplay lcd, AHT20 aht20) {
        this.stateMachine = stateMachine;
        this.lcd = lcd;
        this.aht20 = aht20;
    }

    /**
     * Scheduled method to update the LCD every second.
     */
    @Scheduled(fixedDelay = 1000)
    public void tick(){
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
        lcd.clear();
        lcd.setCursor(0, 0);

        // First line: Show time and state
        String timeStr = java.time.LocalTime.now().withNano(0).toString();
        String stateStr = switch (currentState) {
            case OFF -> "OFF";
            case COOL -> "COOL";
            case HEAT -> "HEAT";
        };
        String line1 = String.format("%-8s %s", timeStr, stateStr);

        String line2 = "";
        if (counter % 5 == 0) {
            // Second line: Show temperature
            counter++;
            line2 = String.format("Temp: %.1fF", temperature);
        } else {
            // TODO: Finish where you left off here. You're alternating the lines from temperature to setpoint every 5 seconds.
            counter = 0;
            // Second line: Show setpoint
        }


        // Print both lines to the LCD
        lcd.print(line1 + "\n" + line2);

        // For debugging purposes, also print to console
        //System.out.println(line1 + " | " + line2);
    }
}
