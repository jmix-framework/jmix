package io.jmix.sessions.endpoint;

import io.jmix.authserver.authentication.RequestLocaleProvider;
import io.jmix.core.security.ClientDetails;
import io.jmix.security.model.SecurityScope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

public class OAuth2TokenEndpointFilterWrapper extends OncePerRequestFilter {
    private OAuth2TokenEndpointFilter delegate;
    private RequestLocaleProvider requestLocaleProvider;

    public OAuth2TokenEndpointFilterWrapper(OAuth2TokenEndpointFilter delegate, RequestLocaleProvider requestLocaleProvider) {
        this.delegate = delegate;
        this.requestLocaleProvider = requestLocaleProvider;
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
