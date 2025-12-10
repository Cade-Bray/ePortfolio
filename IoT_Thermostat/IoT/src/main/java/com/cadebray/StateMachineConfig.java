package com.cadebray;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import java.util.EnumSet;

/**
 * Configuration class for the state machine managing thermostat states.
 * This class defines the states, events, transitions, and listeners for the state machine.
 * Logic for handling state transitions and associated actions is also included here.
 */
@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {
    private final ThermostatProperties thermostatProperties;
    private final LedService ledService;
    private final StateMachine<States, Events> stateMachine;

    /**
     * Constructor for StateMachineConfig
     * @param ledService This is the LED service to control LED indicators
     * @param thermostatProperties This is the ThermostatProperties component
     * @param stateMachine This is the StateMachine instance
     */
    public StateMachineConfig(LedService ledService, ThermostatProperties thermostatProperties,
                              StateMachine<States, Events> stateMachine) {
        this.ledService = ledService;
        this.thermostatProperties = thermostatProperties;
        this.stateMachine = stateMachine;
    }

    /**
     * Configure the state machine settings.
     * @param config The StateMachineConfigurationConfigurer to configure settings
     * @throws Exception If an error occurs during configuration
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    /**
     * Configure the states of the state machine.
     * @param states The StateMachineStateConfigurer to configure states
     * @throws Exception If an error occurs during configuration
     */
    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception{
        states
            .withStates()
            .initial(States.OFF)
            .states(EnumSet.allOf(States.class));
    }

    /**
     * Configure the state machine transitions between states based on events.
     * @param transitions The StateMachineTransitionConfigurer to configure transitions
     * @throws Exception If an error occurs during configuration
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
            // Handle the Off state to Off to Cool transition
            .withExternal()
            .source(States.OFF)
            .target(States.COOL)
            .event(Events.BUTTON_CYCLE)
            // Handle the Cool to Heat transition
            .and()
            .withExternal()
            .source(States.COOL)
            .target(States.HEAT)
            .event(Events.BUTTON_CYCLE)
            // Handle the Heat to Off state transition
            .and()
            .withExternal()
            .source(States.HEAT)
            .target(States.OFF)
            .event(Events.BUTTON_CYCLE);
    }

    /**
     * Create a state machine listener to handle state changes.
     * This listener will invoke the handle method on state changes.
     * @return A StateMachineListener that handles state changes
     */
    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<>() {

            /**
             * Handle state changes in the state machine and perform actions as needed
             * @param from Provide the state we are transitioning from
             * @param to Provide the state we are transitioning to
             */
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                handle(from == null ? null : from.getId(), to == null ? null : to.getId());
            }
        };
    }

    /**
     * Handle state transitions and perform actions based on the new state
     * @param from This is the state we are transitioning from
     * @param to This is the state we are transitioning to
     */
    private void handle(States from, States to) {
        System.out.println("Transitioning from " + from + " to " + to);

        // Per-state actions
        switch (to) {
            case OFF: onEnterOff(); break;
            case COOL: onEnterCool(); break;
            case HEAT: onEnterHeat(); break;
        }
    }

    /**
     * Actions to perform when entering the OFF state
     */
    private void onEnterOff() {
        System.out.println("Entering OFF state actions.");
        ledService.setOff();
    }

    /**
     * Actions to perform when entering the COOL state
     */
    private void onEnterCool() {
        System.out.println("Entering COOL state actions.");
        ledService.onEnterCool();
    }

    /**
     * Actions to perform when entering the HEAT state
     */
    private void onEnterHeat() {
        System.out.println("Entering HEAT state actions.");
        ledService.onEnterHeat();
    }

    /**
     * Get the current thermostat setpoint. This is a wrapper
     * around the ThermostatProperties component because the
     * state machine cannot directly access it.
     * @return The current thermostat setpoint
     */
    public int getSetpoint() {
        return thermostatProperties.getSetpoint();
    }

    /**
     * Decrement the thermostat setpoint by 1 degree
     * @return The new thermostat setpoint
     */
    public int decrementSetpoint() {
        return thermostatProperties.decrementSetpoint();
    }

    /**
     * Increment the thermostat setpoint by 1 degree
     * @return The new thermostat setpoint
     */
    public int incrementSetpoint() {
        return thermostatProperties.incrementSetpoint();
    }

    /**
     * Refresh the LED state based on the current state of the state machine.
     * This can be called periodically to ensure the LED reflects the current state.
     */
    private void refreshLedState() {
        States currentState = stateMachine.getState().getId();
        switch (currentState) {
            case COOL -> ledService.onEnterCool();
            case HEAT -> ledService.onEnterHeat();
            default -> ledService.setOff();
        }
    }
}
