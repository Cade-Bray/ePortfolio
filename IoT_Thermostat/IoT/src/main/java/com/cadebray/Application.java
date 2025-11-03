package com.cadebray;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.statemachine.StateMachine;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final StateMachine<States, Events> stateMachine;

    public Application(StateMachine<States, Events> stateMachine) {
        this.stateMachine = stateMachine;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    @SuppressWarnings("deprecation") // TODO Find the proper implementation that is not depreciated
    public void run(String... args) {
        stateMachine.sendEvent(Events.BUTTON_PRESS);
    }
}