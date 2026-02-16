package com.cadebray;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class sensorPoller {
    private final AHT20 aht20;
    private final ApplicationEventPublisher publisher;
    private final ApiService apiService;

    /**
     * Constructor for the sensor poller
     * @param aht20 This is the sensor itself.
     * @param publisher This is the publisher to be used for publishing events.
     */
    public sensorPoller(AHT20 aht20, ApplicationEventPublisher publisher, ApiService apiService) {
        this.aht20 = aht20;
        this.publisher = publisher;
        this.apiService = apiService;
    }

    /**
     * This is the poller call on a one-second interval. It wraps the sensor value and publishes it for event listeners.
     */
    @Scheduled(fixedDelay = 1000)
    public void poll() {
        double[] reading;
        try {
            reading = aht20.readSensor();
        } catch (Exception e) {
            // Sensor read error, skip this cycle
            return;
        }
        publisher.publishEvent(new SensorReadEvent(reading));
    }

    @Scheduled(fixedDelay = 10000)
    public void pollForRemote() {
        try {
            CurrentState state = new CurrentState();
            state.set_id(apiService.getDeviceId());
            state.setCurrentTemp(aht20.readSensor()[1]);
            apiService.setState(state);
        } catch (Exception _) {}
    }
}
