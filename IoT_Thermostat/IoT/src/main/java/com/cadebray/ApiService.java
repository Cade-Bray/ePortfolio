package com.cadebray;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.*;
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
     * Generate a new authentication token for the device. Can be called to refresh the token when needed,
     * but should be handled automatically within the service.
     */
    public void generateToken() {
        String form = "deviceId=" + URLEncoder.encode(deviceId, StandardCharsets.UTF_8) +
                      "&deviceSecret=" + URLEncoder.encode(deviceSecret, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(form, headers);

        URI uri = resolve("/login");
        ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            this.token = response.getBody();
        } else {
            throw new RuntimeException("Failed to generate token: " + response.getStatusCode());
        }
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
     * Perform a GET request to the specified path with authentication.
     * @param path The relative path for the GET request.
     * @return ResponseEntity containing the response body as a String.
     */
    public ResponseEntity<String> get(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        return restTemplate.exchange(resolve(path), HttpMethod.GET, entity, String.class);
    }

    /**
     * Perform a POST request to the specified path with authentication.
     * @param path The relative path for the POST request.
     * @return ResponseEntity containing the response body as a String.
     */
    public ResponseEntity<String> post(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        return restTemplate.exchange(resolve(path), HttpMethod.POST, entity, String.class);
    }

    /**
     * Perform a PUT request to the specified path with authentication.
     * @param path The relative path for the PUT request.
     * @return ResponseEntity containing the response body as a String.
     */
    public ResponseEntity<String> put(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        return restTemplate.exchange(resolve(path), HttpMethod.PUT, entity, String.class);
    }

    /**
     * Perform a DELETE request to the specified path with authentication.
     * @param path The relative path for the DELETE request.
     * @return ResponseEntity containing the response body as a String.
     */
    public ResponseEntity<String> delete(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        return restTemplate.exchange(resolve(path), HttpMethod.DELETE, entity, String.class);
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
