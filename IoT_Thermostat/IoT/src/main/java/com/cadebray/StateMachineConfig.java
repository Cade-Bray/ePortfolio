package com.cadebray;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {
        config
            .withConfiguration()
            .autoStartup(true)
            .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception{
        states
            .withStates()
            .initial(States.OFF)
            .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
        transitions
            // Handle the Off state to Off to Cool transition
            .withExternal()
            .source(States.OFF)
            .target(States.COOL)
            .event(Events.BUTTON_PRESS)
            // Handle the Cool to Heat transition
            .and()
            .withExternal()
            .source(States.COOL)
            .target(States.HEAT)
            .event(Events.BUTTON_PRESS)
            // Handle the Heat to Off state transition
            .and()
            .withExternal()
            .source(States.HEAT)
            .target(States.OFF)
            .event(Events.BUTTON_PRESS);

        // TODO: Build out transitions for moving from any event to any other event. This will allow ease of transition
        // for the SPA.
    }

    @Bean
    public StateMachineListener<States, Events> listener() {
        return new StateMachineListenerAdapter<States, Events>() {
            @Override
            public void stateChanged(State<States, Events> from, State<States, Events> to) {
                System.out.println("State changed to " + to.getId());
            }
        };
    }
}
