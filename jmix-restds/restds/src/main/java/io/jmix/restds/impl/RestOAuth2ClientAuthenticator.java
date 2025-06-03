package io.jmix.restds.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("restds_RestOAuth2ClientAuthenticator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RestOAuth2ClientAuthenticator implements RestAuthenticator {

    @Autowired
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired
    private Environment environment;

    private String oauth2ClientRegistration;

    @Override
    public void setDataStoreName(String name) {
        oauth2ClientRegistration = environment.getRequiredProperty(name + ".oauth2-client-registration");
    }

    @Override
    public ClientHttpRequestInterceptor getAuthenticationInterceptor() {
        return new AuthenticatingClientHttpRequestInterceptor();
    }

    private String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Cannot get access token: Authentication object is null");
        }

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(oauth2ClientRegistration)
                .principal(authentication)
                .build();

        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        if (authorizedClient == null) {
            throw new IllegalStateException("Cannot authorize " + authorizeRequest);
        }
        return authorizedClient.getAccessToken().getTokenValue();
    }

    private class AuthenticatingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().setBearerAuth(getAccessToken());
            return execution.execute(request, body);
        }
    }
}
