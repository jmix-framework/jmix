package io.jmix.authserver.authentication;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

/**
 * The class configures the token endpoint by registering
 * {@link OAuth2ResourceOwnerPasswordCredentialsAuthenticationConverter} and
 * {@link OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider} which are required for OAuth 2.0 Resource Owner
 * Password Credentials grant.
 *
 * @see OAuth2ResourceOwnerPasswordCredentialsAuthenticationConverter
 * @see OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider
 */
public class OAuth2ResourceOwnerPasswordTokenEndpointConfigurer extends AbstractHttpConfigurer<OAuth2ResourceOwnerPasswordTokenEndpointConfigurer, HttpSecurity> {

    @Override
    public void init(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        authorizationServerConfigurer.tokenEndpoint(tokenEndpoint ->
                tokenEndpoint.accessTokenRequestConverter(new OAuth2ResourceOwnerPasswordCredentialsAuthenticationConverter())
        );
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        //we create token OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider here because OAuth2TokenGenerator
        //is placed into shared objects only in {@code OAuth2TokenEndpointConfigurer#init} method.
        OAuth2TokenGenerator<?> tokenGenerator = http.getSharedObject(OAuth2TokenGenerator.class);
        ApplicationContext applicationContext = http.getSharedObject(ApplicationContext.class);
        AuthenticationManager authenticationManager = applicationContext.getBean(AuthenticationManager.class);
        OAuth2AuthorizationService oAuth2AuthorizationService = applicationContext.getBean(OAuth2AuthorizationService.class);

        OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider authenticationProvider =
                new OAuth2ResourceOwnerPasswordCredentialsAuthenticationProvider(tokenGenerator, authenticationManager, oAuth2AuthorizationService);
        http.authenticationProvider(authenticationProvider);
    }
}
