package com.cadebray;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service to manage LED indicators for thermostat states.
 * Controls red and blue LEDs based on heating and cooling states.
 */
@Component
public class LedService {
    private final Context pi4j;
    private final AHT20 aht20;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ThermostatProperties thermostatProperties;
    private final ObjectFactory<StateMachine<States, Events>> stateMachineFactory;
    private DigitalOutput redLed;
    private DigitalOutput blueLed;
    private ScheduledFuture<?> pulseTask;
    private final AtomicBoolean pulsing = new AtomicBoolean(false);
    private final Object pulseLock = new Object();

    /**
     * Constructor for LedService.
     * @param pi4j Pi4J Context for GPIO interactions
     * @param aht20 AHT20 sensor for temperature readings
     */
    public LedService(Context pi4j, AHT20 aht20, ThermostatProperties thermostatProperties,
                      ObjectFactory<StateMachine<States, Events>> stateMachineFactory) {
        this.thermostatProperties = thermostatProperties;
        this.pi4j = pi4j;
        this.aht20 = aht20;
        this.stateMachineFactory = stateMachineFactory;
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
        synchronized (pulseLock) {
            if (redLed != null) redLed.low();
            if (blueLed != null) blueLed.low();
        }
    }

    /**
     * Handle entering the heating state. Turns on red LED or pulses it based on temperature.
     */
    public synchronized void onEnterHeat() {
        stopPulse();
        double tempF = Double.NaN;
        double setpoint = thermostatProperties.getSetpoint();
        try {
            tempF = aht20.readSensor()[1];
            System.out.println("Heat check: temp=" + tempF + " setpoint=" + setpoint);
        } catch (Exception e) {
            System.err.println("Error reading temperature: " + e.getMessage());
        }
        updateHeatForTemp(tempF, setpoint);
        synchronized (pulseLock) {
            if (blueLed != null) blueLed.low();
        }
    }

    /**
     * Handle entering the cooling state. Turns on blue LED or pulses it based on temperature.
     * TODO: Refactor duplicate code with onEnterHeat(). See if I can combine them into a single function.
     */
    public synchronized void onEnterCool() {
        stopPulse();
        double tempF = Double.NaN;
        double setpoint = thermostatProperties.getSetpoint();
        try {
            tempF = aht20.readSensor()[1];
            System.out.println("Cool check: temp=" + tempF + " setpoint=" + setpoint);
        } catch (Exception e) {
            System.err.println("Error reading temperature: " + e.getMessage());
        }
        updateCoolForTemp(tempF, setpoint);
        synchronized (pulseLock) {
            if (redLed != null) redLed.low();
        }
    }

    /**
     * Start pulsing the given LED at a fixed interval.
     * @param led The DigitalOutput LED to pulse
     */
    private void startPulse(DigitalOutput led) {
        if (led == null) return;
        // stop any existing pulse first
        stopPulse();
        pulsing.set(true);
        // schedule with an initial delay to reduce immediate race with subsequent state changes
        pulseTask = scheduler.scheduleAtFixedRate(() -> {
            if (!pulsing.get()) return;
            synchronized (pulseLock) {
                try {
                    if (led.state().isHigh()) {
                        led.low();
                    } else {
                        led.high();
                    }
                } catch (Exception ignored) {}
            }
        }, 600, 600, TimeUnit.MILLISECONDS);
    }

    /**
     * Stop any ongoing pulsing of LEDs.
     */
    private void stopPulse() {
        // clear flag first so any running task will skip toggling
        pulsing.set(false);
        if (pulseTask != null) {
            try {
                pulseTask.cancel(true);
            } catch (Exception ignored) {}
            pulseTask = null;
        }
    }

    /**
     * Event listener for temperature crossing events.
     * @param measuredTemp The measured temperature
     */
    public synchronized void onTemperatureCrossing(double measuredTemp) {
        double setpoint = thermostatProperties.getSetpoint();
        StateMachine<States, Events> sm = stateMachineFactory.getObject();
        if (sm.getState() == null) return;
        States current = sm.getState().getId();
        switch (current) {
            case HEAT -> updateHeatForTemp(measuredTemp, setpoint);
            case COOL -> updateCoolForTemp(measuredTemp, setpoint);
            default -> setOff();
        }
    }

    /**
     * Update the heating LED based on the current temperature and setpoint.
     * @param tempF Current temperature in Fahrenheit
     * @param setpoint Desired temperature setpoint
     */
    private void updateHeatForTemp(double tempF, double setpoint) {
        if (!Double.isNaN(tempF)) {
            if (tempF >= setpoint) {
                // reached or above setpoint -> steady RED on
                stopPulse();
                synchronized (pulseLock) {
                    if (redLed != null) redLed.high();
                }
            } else {
                // below setpoint -> pulse RED
                synchronized (pulseLock) {
                    if (redLed != null) redLed.low();
                    startPulse(redLed);
                }
            }
        } else {
            synchronized (pulseLock) {
                if (redLed != null) redLed.low();
                stopPulse();
            }
        }
    }

    /**
     * Update the cooling LED based on the current temperature and setpoint.
     * @param tempF Current temperature in Fahrenheit
     * @param setpoint Desired temperature setpoint
     */
    private void updateCoolForTemp(double tempF, double setpoint) {
        if (!Double.isNaN(tempF)) {
            if (tempF <= setpoint) {
                // reached or below setpoint -> steady BLUE on
                stopPulse();
                synchronized (pulseLock) {
                    if (blueLed != null) blueLed.high();
                }
            } else {
                // above setpoint -> pulse BLUE
                synchronized (pulseLock) {
                    if (blueLed != null) blueLed.low();
                    startPulse(blueLed);
                }
            }
        } else {
            synchronized (pulseLock) {
                if (blueLed != null) blueLed.low();
                stopPulse();
            }
        }
    }
}
