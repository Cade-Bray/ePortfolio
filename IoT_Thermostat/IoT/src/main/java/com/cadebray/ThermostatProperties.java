package com.cadebray;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
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

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private ApiService apiService;
    private final ObjectFactory<StateMachine<States, Events>> stateMachineFactory;

    @Autowired
    public ThermostatProperties(ObjectFactory<StateMachine<States, Events>> stateMachineFactory){
        this.stateMachineFactory = stateMachineFactory;
    }

    private StateMachine<States, Events> getStateMachine() {
        return stateMachineFactory.getObject();
    }

    /**
     * Set the temperature setpoint.
     * @param setpoint The desired temperature setpoint
     */
    public synchronized void setSetpoint(Double setpoint) {
        if (setpoint == null) {
            return;
        }
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

    @Scheduled(fixedDelay = 15000)
    public void refreshState(){
        CurrentState current_state = apiService.getState();
        setSetpoint(current_state.getSetTemp());

        States remote_state;
        switch (current_state.getState()) {
            case "HEAT": remote_state = States.HEAT; break;
            case "COOL": remote_state = States.COOL; break;
            case "OFF": remote_state = States.OFF; break;
            default: return;
        }

        int counter = 0;
        while (counter <= States.values().length) {
            if (getStateMachine().getState().getId() == remote_state){
                break;
            }

            Message<Events> msg = MessageBuilder.withPayload(Events.BUTTON_CYCLE).build();

            getStateMachine().sendEvent(Mono.just(msg)).subscribe(
                    null,
                    error -> System.err.println("Error sending event to state machine: " + error)
            );
            counter++;
        }
    }
}
