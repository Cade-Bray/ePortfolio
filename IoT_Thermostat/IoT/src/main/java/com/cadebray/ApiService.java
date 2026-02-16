package com.cadebray;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@SuppressWarnings("unused") // TODO remove unused warnings when methods are used and I can be sure they are needed
@ConfigurationProperties(prefix = "api")
public class ApiService {
    private final RestTemplate restTemplate;
    private String token;
    private URI rootAddress;
    private String deviceId;
    private String deviceSecret;

    /**
     * Constructor for ApiService class with specified root address.
     */
    public ApiService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Generate a new login request object for the device. Can be called to refresh the token when needed,
     * but should be handled automatically with the scheduler.
     */
    @Scheduled(fixedDelay = 50000)
    public void generateLogin() {
        String form = "deviceId=" + URLEncoder.encode(deviceId, StandardCharsets.UTF_8) +
                "&secret=" + URLEncoder.encode(deviceSecret, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(form, headers);

        URI uri = resolve("/api/iot/login");
        ResponseEntity<loginResponse> response = restTemplate.postForEntity(uri, request, loginResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            this.token = response.getBody().getToken();
        } else {
            throw new RuntimeException("Failed to generate login object: " + response.getStatusCode());
        }
    }

    /**
     * This is the get state request. This function will get the current remote state of the thermostat.
     * @return Returns a CurrentState class object that has all the appropriate fields.
     */
    public CurrentState getState(){
        HttpEntity<String> request = new HttpEntity<>(authHeaders());

        URI uri = resolve("/api/iot/" + getDeviceId());
        ResponseEntity<CurrentState> response = restTemplate.exchange(uri, HttpMethod.GET, request, CurrentState.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get current state: " + response.getStatusCode());
        }
    }

    /**
     * This function will make the post request to update the thermostat remote state. This should only be called on
     * button presses.
     * @param state
     */
    public void setState(CurrentState state){
        HttpEntity<CurrentState> request = new HttpEntity<>(state, authHeaders());

        URI uri = resolve("/api/iot/" + getDeviceId());
        ResponseEntity<CurrentState> response = restTemplate.exchange(
                uri,
                HttpMethod.PUT,
                request,
                CurrentState.class
        );
    }

    /**
     * Resolve a relative path against the root address.
     * @param path The relative path to resolve.
     * @return The resolved URI.
     */
    private URI resolve(String path) {
        String normalized = path.startsWith("/") ? path : "/" + path;
        return rootAddress.resolve(normalized);
    }

    /**
     * Create HTTP headers with authentication token.
     * @return HttpHeaders with Bearer token if available.
     */
    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // Set content type to JSON
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Add Bearer token if available
        if (token != null) {
            headers.setBearerAuth(token);
        }

        return headers;
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
