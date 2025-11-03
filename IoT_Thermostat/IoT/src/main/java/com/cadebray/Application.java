package com.cadebray;
import com.pi4j.Pi4J;
import com.pi4j.util.Console;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.statemachine.StateMachine;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final StateMachine<States, Events> stateMachine;
    private Console console;

    public Application(StateMachine<States, Events> stateMachine) {
        // State Machine Initializer
        this.stateMachine = stateMachine;

        // Pi4J Initializer
        this.console = new Console();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        var pi4j = Pi4J.newAutoContext();
    }

    @Override
    @SuppressWarnings("deprecation") // TODO Find the proper implementation that is not depreciated
    public void run(String... args) {
        stateMachine.sendEvent(Events.BUTTON_PRESS);
    }
}