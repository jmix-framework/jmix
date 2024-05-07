package io.jmix.superset.model;

import java.io.Serializable;

public class GuestTokenResponse implements Serializable {

    private String token;
    private String message; // when error occurs
    private String msg; // when token is expired

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
