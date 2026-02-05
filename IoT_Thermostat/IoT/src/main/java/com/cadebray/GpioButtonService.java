package com.cadebray;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalInputConfig;
import com.pi4j.io.gpio.digital.DigitalStateChangeListener;
import com.pi4j.io.gpio.digital.PullResistance;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Component that manages GPIO buttons and sends events to the state machine
 * when buttons are pressed.
 */
@Component
public class GpioButtonService {
    private final Context pi4j;
    private DigitalInput cycleButton;
    private DigitalInput raiseButton;
    private DigitalInput lowerButton;
    private DigitalStateChangeListener buttonListener;
    private final ObjectFactory<StateMachine<States, Events>> stateMachineFactory;

    /**
     * Constructor for GpioButtonServiceComponent. This component manages GPIO buttons
     * and sends events to the state machine when buttons are pressed.
     * @param pi4j The Pi4J context for GPIO interactions
     * @param stateMachine The state machine to send events to
     */
    public GpioButtonService(Context pi4j, ObjectFactory<StateMachine<States, Events>> stateMachine) {
        this.pi4j = pi4j;
        stateMachineFactory = stateMachine;
    }

    /**
     * Get the state machine instance from the factory.
     * @return The state machine instance
     */
    private StateMachine<States, Events> getStateMachine() {
        return stateMachineFactory.getObject();
    }

    /**
     * Initialize the GPIO buttons and register listeners.
     * Uses the PostConstruct annotation to ensure this method is called after
     * the component is constructed.
     */
    @PostConstruct
    public void initialize(){
        // Create digital inputs for each button on their addressed GPIO pins
        cycleButton = createInput("button-cycle", 24);
        raiseButton = createInput("button-raise", 25);
        lowerButton = createInput("button-lower", 12);

        // Define a common listener for button state changes.

        buttonListener = event -> {
            // assume active-low buttons; ignore releases
            if (!event.state().isLow()) return;

            // Determine which button was pressed and create corresponding event
            Events payload;
            String id = event.source().id();
            switch (id) {
                case "button-cycle" -> payload = Events.BUTTON_CYCLE;
                case "button-raise" -> payload = Events.BUTTON_RAISE;
                case "button-lower" -> payload = Events.BUTTON_LOWER;
                case null, default -> {
                    return; // Unknown button, ignore it
                }
            }

            // Build and send the event to the state machine. Uses msg and Mono for reactive handling.
            Message<Events> msg = MessageBuilder.withPayload(payload).build();

            getStateMachine().sendEvent(Mono.just(msg)).subscribe(
                    null,
                    error -> System.err.println("Error sending event to state machine: " + error)
            );
        };

        // register listener on each input
        cycleButton.addListener(buttonListener);
        raiseButton.addListener(buttonListener);
        lowerButton.addListener(buttonListener);
    }

    /**
     * Create a digital input pin with pull-up resistor and debounce
     * @param id This is the unique identifier for the pin
     * @param address This is the GPIO address for the pin
     * @return The created DigitalInput pin
     */
    private DigitalInput createInput(String id, int address) {
        DigitalInputConfig config = DigitalInput
                .newConfigBuilder(pi4j) // Create a new DigitalInputConfigBuilder
                .id(id) // Set the unique identifier
                .name(id) // Set the human-readable name
                .address(address) // Set the GPIO address
                .pull(PullResistance.PULL_UP) // Enable pull-up resistor which is typical for buttons
                .debounce(50L) // Set debounce time to 50 milliseconds to avoid multiple triggers
                .build(); // Build the configuration

        return pi4j.create(config, DigitalInput.class);
    }

    /**
     * Shutdown the GPIO inputs and remove listeners to avoid memory leaks.
     */
    @PreDestroy
    public void shutdown(){
        try {
            if (buttonListener != null) {
                // The buttonListener may be null if initialization failed
                // Remove listeners to avoid memory leaks
                cycleButton.removeListener(buttonListener);
                raiseButton.removeListener(buttonListener);
                lowerButton.removeListener(buttonListener);
            }
            if (cycleButton != null) {
                // The cycleButton may be null if initialization failed
                // Shutdown each input
                cycleButton.shutdown(pi4j);
            }
            if (raiseButton != null) {
                // The raiseButton may be null if initialization failed
                // Shutdown each input
                raiseButton.shutdown(pi4j);
            }
            if (lowerButton != null) {
                // The lowerButton may be null if initialization failed
                // Shutdown each input
                lowerButton.shutdown(pi4j);
            }
        } catch (Exception e) {
            // Ignore exceptions during shutdown
        }
    }
}