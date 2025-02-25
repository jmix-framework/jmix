package io.jmix.sessions.endpoint;

import io.jmix.authserver.authentication.RequestLocaleProvider;
import io.jmix.core.session.SessionData;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.List;
import java.util.Optional;

@Component
public class TokenEndpointFilterCustomizationBean {

    @Autowired
    @Qualifier("authsr_AuthorizationServerSecurityFilterChain")
    private SecurityFilterChain authorizationServerSecurityFilterChain;

    @Autowired
    private RequestLocaleProvider requestLocaleProvider;

    @Autowired
    private OAuth2AuthorizationService authorizationService;

    @Autowired
    private ObjectProvider<SessionData> sessionDataProvider;

    @PostConstruct
    public void modifyFilterChain() {
        Optional<OAuth2TokenEndpointFilter> tokenEndpointFilter = authorizationServerSecurityFilterChain.getFilters().stream()
                .filter(filter -> OAuth2TokenEndpointFilter.class.isAssignableFrom(filter.getClass()))
                .map(f -> (OAuth2TokenEndpointFilter) f)
                .findAny();

        if (tokenEndpointFilter.isEmpty()) {
            throw new RuntimeException("No OAuth2TokenEndpointFilter found");
        }

        List<Filter> filters = authorizationServerSecurityFilterChain.getFilters();
        filters.replaceAll(filter -> {
            if (filter instanceof OAuth2TokenEndpointFilter delegate) {
                return createWrapper(delegate);
            } else {
                return filter;
            }
        });
    }

    protected OncePerRequestFilter createWrapper(OAuth2TokenEndpointFilter delegate){
        return new OAuth2TokenEndpointFilterWrapper(delegate, requestLocaleProvider, sessionDataProvider, authorizationService);
    }
}