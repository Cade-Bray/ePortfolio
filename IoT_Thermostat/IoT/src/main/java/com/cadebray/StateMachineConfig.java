package com.cadebray;
import org.springframework.beans.factory.ObjectFactory;
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
@SuppressWarnings("unused") // Suppress unused warnings for Spring configuration classes TODO remove before release
@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {
    private final ThermostatProperties thermostatProperties;
    private final LedService ledService;
    private final ObjectFactory<StateMachine<States, Events>> stateMachineFactory;

    /**
     * Constructor for StateMachineConfig
     * @param ledService This is the LED service to control LED indicators
     * @param thermostatProperties This is the ThermostatProperties component
     * @param stateMachine This is the StateMachine instance
     */
    public StateMachineConfig(LedService ledService, ThermostatProperties thermostatProperties,
                              ObjectFactory<StateMachine<States, Events>> stateMachine) {
        this.ledService = ledService;
        this.thermostatProperties = thermostatProperties;
        this.stateMachineFactory = stateMachine;
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

        // Handle raise/lower events as internal transitions (no state change) so setpoint updates occur
        // OFF
        transitions
            .withInternal()
            .source(States.OFF)
            .event(Events.BUTTON_RAISE)
            .action(ctx -> {
                double newVal = incrementSetpoint();
                System.out.println("Setpoint incremented (OFF) -> " + newVal);
            })
            .and()
            .withInternal()
            .source(States.OFF)
            .event(Events.BUTTON_LOWER)
            .action(ctx -> {
                double newVal = decrementSetpoint();
                System.out.println("Setpoint decremented (OFF) -> " + newVal);
            });

        // COOL
        transitions
            .withInternal()
            .source(States.COOL)
            .event(Events.BUTTON_RAISE)
            .action(ctx -> {
                double newVal = incrementSetpoint();
                System.out.println("Setpoint incremented (COOL) -> " + newVal);
            })
            .and()
            .withInternal()
            .source(States.COOL)
            .event(Events.BUTTON_LOWER)
            .action(ctx -> {
                double newVal = decrementSetpoint();
                System.out.println("Setpoint decremented (COOL) -> " + newVal);
            });

        // HEAT
        transitions
            .withInternal()
            .source(States.HEAT)
            .event(Events.BUTTON_RAISE)
            .action(ctx -> {
                double newVal = incrementSetpoint();
                System.out.println("Setpoint incremented (HEAT) -> " + newVal);
            })
            .and()
            .withInternal()
            .source(States.HEAT)
            .event(Events.BUTTON_LOWER)
            .action(ctx -> {
                double newVal = decrementSetpoint();
                System.out.println("Setpoint decremented (HEAT) -> " + newVal);
            });
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
                handle(from == null ? null : from.getId(), to == null ? States.OFF : to.getId());
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
        ledService.setOff();
    }

    /**
     * Actions to perform when entering the COOL state
     */
    private void onEnterCool() {
        ledService.onEnterCool();
    }

    /**
     * Actions to perform when entering the HEAT state
     */
    private void onEnterHeat() {
        ledService.onEnterHeat();
    }

    /**
     * Get the current thermostat setpoint. This is a wrapper
     * around the ThermostatProperties component because the
     * state machine cannot directly access it.
     * @return The current thermostat setpoint
     */
    public double getSetpoint() {
        return thermostatProperties.getSetpoint();
    }

    /**
     * Decrement the thermostat setpoint by 1 degree
     * @return The new thermostat setpoint
     */
    public double decrementSetpoint() {
        return thermostatProperties.decrementSetpoint();
    }

    /**
     * Increment the thermostat setpoint by 1 degree
     * @return The new thermostat setpoint
     */
    public double incrementSetpoint() {
        return thermostatProperties.incrementSetpoint();
    }

    private StateMachine<States, Events> getStateMachine() {
        return stateMachineFactory.getObject();
    }

    /**
     * Refresh the LED state based on the current state of the state machine.
     * This can be called periodically to ensure the LED reflects the current state.
     */
    private void refreshLedState() {
        // Get the current state from the state machine
        States currentState = getStateMachine().getState().getId();
        switch (currentState) {
            case COOL -> ledService.onEnterCool();
            case HEAT -> ledService.onEnterHeat();
            default -> ledService.setOff();
        }
    }
}
