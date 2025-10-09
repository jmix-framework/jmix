package io.jmix.email.authentication;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

/**
 * {@link Authenticator} implementation that uses access token provided by {@link OAuth2TokenProvider}.
 */
public class OAuth2Authenticator extends Authenticator {

    private final String username;
    private final OAuth2TokenProvider tokenProvider;

    public OAuth2Authenticator(String username, OAuth2TokenProvider tokenProvider) {
        this.username = username;
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, tokenProvider.getAccessToken());
    }
}
