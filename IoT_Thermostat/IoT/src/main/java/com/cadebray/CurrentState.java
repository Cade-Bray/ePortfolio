package com.cadebray;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrentState {
    @JsonProperty("_id")
    private String _id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("state")
    private String state;

    @JsonProperty("setTemp")
    private Double setTemp;

    @JsonProperty("currentTemp")
    private Double currentTemp;

    @JsonProperty("lastChecked")
    private String lastChecked;

    @JsonProperty("auth_users")
    private String[] auth_users;

    public CurrentState(){}

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getSetTemp() {
        return setTemp;
    }

    public void setSetTemp(Double setTemp) {
        this.setTemp = setTemp;
    }

    public Double getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(Double currentTemp) {
        this.currentTemp = currentTemp;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }

    public String[] getAuth_users() {
        return auth_users;
    }

    public void setAuth_users(String[] auth_users) {
        this.auth_users = auth_users;
    }
}
