package com.cadebray;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Thermostat system.
 * This class initializes the Spring Boot application and configures necessary beans.
 */
@SpringBootApplication
@EnableScheduling
public class Application {

    /**
     * Main method to start the Spring Boot application.
     * @param args Command-line arguments
     */
    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Create and configure the Pi4J context bean.
     * This will be used for GPIO and sensor interactions.
     * @return The initialized Pi4J context
     */
    @Bean
    public Context pi4j(){
        return Pi4J.newAutoContext();
    }

    /**
     * Create the LCDisplay bean.
     * This bean will handle interactions with the LCD tasks.
     * @param pi4j The Pi4J context for GPIO interactions
     * @return The initialized LCDisplay instance
     */
    @Bean
    public LCDisplay lcd(Context pi4j) {
        return new LCDisplay(pi4j);
    }

    /**
     * Create the AHT20 sensor bean.
     * This bean will handle interactions with the AHT20 temperature and humidity sensor.
     * TODO: I don't think I need this as a bean, but leaving for now.
     * @return The initialized AHT20 instance
     */
    @Bean
    public AHT20 aht20() {
        return new AHT20();
    }
}