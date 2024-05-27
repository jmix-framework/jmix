package io.jmix.authserver.authentication;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

/**
 * The class configures the token endpoint
 * by registering {@link OAuth2ResourceOwnerPasswordCredentialsAuthenticationConverter} and
 * {@link OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider} which are required for OAuth 2.0 Resource Owner
 * Password Credentials grant.
 *
 * @see OAuth2ResourceOwnerPasswordCredentialsAuthenticationConverter
 * @see OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider
 */
@Component
public class OAuth2ResourceOwnerPasswordTokenEndpointConfigurer extends AbstractHttpConfigurer<OAuth2ResourceOwnerPasswordTokenEndpointConfigurer, HttpSecurity> {

    @Override
    public void init(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        http.with(authorizationServerConfigurer, configurer -> {
            OAuth2TokenGenerator<?> tokenGenerator = http.getSharedObject(OAuth2TokenGenerator.class);
            ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
            AuthenticationManager authenticationManager = applicationContext.getBean(AuthenticationManager.class);
            OAuth2AuthorizationService oAuth2AuthorizationService = applicationContext.getBean(OAuth2AuthorizationService.class);

            configurer
                    .tokenEndpoint(tokenEndpoint ->
                            tokenEndpoint
                                    .accessTokenRequestConverter(
                                            new OAuth2ResourceOwnerPasswordCredentialsAuthenticationConverter())
                                    .authenticationProvider(
                                            new OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider(
                                                    tokenGenerator, authenticationManager, oAuth2AuthorizationService))
                    );
        });
    }
}
