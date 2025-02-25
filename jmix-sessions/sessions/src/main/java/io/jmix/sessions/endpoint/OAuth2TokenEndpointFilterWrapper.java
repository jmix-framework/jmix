package io.jmix.sessions.endpoint;

import io.jmix.authserver.authentication.RequestLocaleProvider;
import io.jmix.authserver.service.cleanup.impl.UserInvalidationListener;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.session.SessionData;
import io.jmix.security.model.SecurityScope;
import io.jmix.sessions.resolver.OAuth2AndCookieSessionIdResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AccessTokenResponseAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

public class OAuth2TokenEndpointFilterWrapper extends OncePerRequestFilter {
    protected OAuth2TokenEndpointFilter delegate;
    protected RequestLocaleProvider requestLocaleProvider;
    protected ObjectProvider<SessionData> sessionDataProvider;
    protected OAuth2AuthorizationService authorizationService;

    public OAuth2TokenEndpointFilterWrapper(OAuth2TokenEndpointFilter delegate,
                                            RequestLocaleProvider requestLocaleProvider,
                                            ObjectProvider<SessionData> sessionDataProvider,
                                            OAuth2AuthorizationService authorizationService) {
        this.delegate = delegate;
        this.requestLocaleProvider = requestLocaleProvider;
        this.sessionDataProvider = sessionDataProvider;
        this.authorizationService = authorizationService;

        delegate.setAuthenticationSuccessHandler(createSuccessHandler());
    }

    protected AuthenticationSuccessHandler createSuccessHandler(){
        OAuth2AccessTokenResponseAuthenticationSuccessHandler successHandler = new OAuth2AccessTokenResponseAuthenticationSuccessHandler();
        successHandler.setAccessTokenResponseCustomizer(c -> {
            Authentication authentication = c.get(Authentication.class);
            if (authentication instanceof OAuth2AccessTokenAuthenticationToken authToken) {
                SessionData sessionData = sessionDataProvider.getObject();
                sessionData.setAttribute(
                        OAuth2AndCookieSessionIdResolver.ACCESS_TOKEN,
                        authToken.getAccessToken().getTokenValue());

                OAuth2Authorization auth = authorizationService.findByToken(
                        authToken.getAccessToken().getTokenValue(),
                        OAuth2TokenType.ACCESS_TOKEN);

                if (auth != null) {
                    OAuth2Authorization updated = OAuth2Authorization.from(auth)
                            .attribute(OAuth2AndCookieSessionIdResolver.SESSION_ID, sessionData.getSessionId())
                            .attribute(UserInvalidationListener.AUTHORIZATION_ID, auth.getId())
                            .build();
                    authorizationService.save(updated);
                }
            }
        });
        return successHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication instanceof OAuth2ClientAuthenticationToken t) {
                ClientDetails details = ClientDetails.builder()
                        .clientType("API")
                        .scope(SecurityScope.API)
                        .locale(requestLocaleProvider.getLocale(request))
                        .sessionId(request.getSession().getId())
                        .build();
                t.setDetails(details);

            }
        }
        delegate.doFilter(request, response, filterChain);
    }
}
