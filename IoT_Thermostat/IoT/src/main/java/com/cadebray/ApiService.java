package com.cadebray;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.BodyInserters;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@ConfigurationProperties(prefix = "api")
public class ApiService {
    private final WebClient client;
    private String token;
    private URI rootAddress;
    private String deviceId;
    private String deviceSecret;


    /**
     * Constructor for ApiService class with specified root address.
     */
    public ApiService() {
        this.client = WebClient.create();
    }

    /**
     * Generate a new authentication token for the device. Can be called to refresh the token when needed,
     * but should be handled automatically within the service.
     */
    public void generateToken() {
        String form = "deviceId=" + URLEncoder.encode(this.deviceId, StandardCharsets.UTF_8)
                + "&deviceSecret=" + URLEncoder.encode(this.deviceSecret, StandardCharsets.UTF_8);

        // Get response from the login endpoint and check status code token will be in the response body
        ResponseEntity<String> response = this.client.post()
                .uri(this.rootAddress + "/auth/device/login")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromValue(form))
                .retrieve()
                .toEntity(String.class)
                .block();

        // Check for null response.
        if (response == null) {
            throw new IllegalStateException("No response from server when generating token.");
        }

        // Check status code and handle accordingly
        if (response.getStatusCode().is2xxSuccessful()) {
            this.token = response.getBody();
        } else {
            throw new IllegalStateException("Failed to generate token: " + response.getStatusCode());
        }
    }

    /**
     * Set the device secret used for authentication. This is a Spring Boot
     * configuration property and will be set automatically from application properties.
     * @param deviceSecret The device secret string.
     */
    public void setDeviceSecret(String deviceSecret) {
        this.deviceSecret = deviceSecret;
    }

    /**
     * Get the device secret used for authentication.
     * @return The device secret string.
     */
    public String getDeviceSecret() {
        return deviceSecret;
    }

    /**
     * Set the root address for the API service. This is a Spring Boot
     * configuration property and will be set automatically from application properties.
     * Ensure that your application.properties file contains the correct value because credentials
     * are going to be url encoded to this address. The function
     * @param rootAddress The root address string.
     */
    public void setRootAddress(String rootAddress) {
        this.rootAddress = URI.create(rootAddress);
    }

    /**
     * Get the root address for the API service.
     * @return The root address string.
     */
    public URI getRootAddress() {
        return rootAddress;
    }

    /**
     * Set the device ID for the API service. This is a Spring Boot
     * configuration property and will be set automatically from application properties.
     * @param deviceId The device ID string.
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get the device ID for the API service.
     * @return The device ID string.
     */
    public String getDeviceId() {
        return deviceId;
    }
}
