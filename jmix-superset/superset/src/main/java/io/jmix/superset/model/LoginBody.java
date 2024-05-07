package io.jmix.superset.model;

import java.io.Serializable;

public class LoginBody implements Serializable {

    private String username;
    private String password;
    private String provider;
    private Boolean refresh;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LoginBody withUsername(String username) {
        setUsername(username);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LoginBody withPassword(String password) {
        setPassword(password);
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public LoginBody withProvider(String provider) {
        setProvider(provider);
        return this;
    }

    public Boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }

    public LoginBody withRefresh(Boolean refresh) {
        setRefresh(refresh);
        return this;
    }
}
