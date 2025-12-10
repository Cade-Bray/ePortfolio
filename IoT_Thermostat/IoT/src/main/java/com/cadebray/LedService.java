package com.cadebray;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service to manage LED indicators for thermostat states.
 * Controls red and blue LEDs based on heating and cooling states.
 */
@Component
public class LedService {
    private final Context pi4j;
    private final AHT20 aht20;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private DigitalOutput redLed;
    private DigitalOutput blueLed;
    private ScheduledFuture<?> pulseTask;

    // Thermostat setpoint temperature. Uses @Value to inject from application properties.
    @Value("${thermostat.setpoint:72}")
    private int setpoint;

    /**
     * Constructor for LedService.
     * @param pi4j Pi4J Context for GPIO interactions
     * @param aht20 AHT20 sensor for temperature readings
     */
    public LedService(Context pi4j, AHT20 aht20) {
        this.pi4j = pi4j;
        this.aht20 = aht20;
    }

    /**
     * Initialize the LED GPIO outputs. Uses @PostConstruct to set up after construction.
     */
    @PostConstruct
    public void initialize() {
        // Setup red LED on GPIO pin 18
        DigitalOutputConfig redConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("red-led")
                .name("Red LED")
                .address(18) // GPIO pin 18
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .build();

        // Setup blue LED on GPIO pin 23
        DigitalOutputConfig blueConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("blue-led")
                .name("Blue LED")
                .address(23) // GPIO pin 23
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW)
                .build();

        redLed = pi4j.create(redConfig, DigitalOutput.class);
        blueLed = pi4j.create(blueConfig, DigitalOutput.class);
    }

    /**
     * Shutdown the LED service and clean up resources. Uses @PreDestroy to clean up before destruction.
     */
    @PreDestroy
    public void shutdown() {
        stopPulse();
        try {
            if (redLed != null) redLed.shutdown(pi4j);
            if (blueLed != null) blueLed.shutdown(pi4j);
        } catch (Exception e) {
            System.err.println("Error shutting down LEDs: " + e.getMessage());
        }
        scheduler.shutdownNow();
    }

    /**
     * Set both LEDs off and stop any pulsing.
     */
    public synchronized void setOff() {
        stopPulse();
        redLed.low();
        blueLed.low();
    }

    /**
     * Handle entering the heating state. Turns on red LED or pulses it based on temperature.
     */
    public synchronized void onEnterHeat() {
        stopPulse();
        double tempF = 0;
        try {
            tempF = aht20.readSensor()[1];
        } catch (Exception e) {
            System.err.println("Error reading temperature: " + e.getMessage());
        }
        if (tempF >= setpoint) {
            redLed.high();
        } else {
            redLed.low();
            startPulse(redLed);
        }
        blueLed.low();
    }

    /**
     * Handle entering the cooling state. Turns on blue LED or pulses it based on temperature.
     * TODO: Refactor duplicate code with onEnterHeat(). See if I can combine them into a single function.
     */
    public synchronized void onEnterCool() {
        stopPulse();
        double tempF = 0;
        try {
            tempF = aht20.readSensor()[1];
        } catch (Exception e) {
            System.err.println("Error reading temperature: " + e.getMessage());
        }
        if (tempF <= setpoint) {
            // solid blue
            blueLed.high();
        } else {
            // pulse blue
            blueLed.low();
            startPulse(blueLed);
        }
        redLed.low();
    }

    /**
     * Start pulsing the given LED at a fixed interval.
     * @param led The DigitalOutput LED to pulse
     */
    private void startPulse(DigitalOutput led) {
        stopPulse();
        pulseTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                if (led.state().isHigh()) {
                    led.low();
                } else {
                    led.high();
                }
            } catch (Exception ignored) {}
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop any ongoing pulsing of LEDs.
     */
    private void stopPulse() {
        if (pulseTask != null) {
            pulseTask.cancel(true);
            pulseTask = null;
        }
    }
}
