package io.jmix.sessions.endpoint;

import io.jmix.authserver.authentication.RequestLocaleProvider;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.server.authorization.web.OAuth2TokenEndpointFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TokenEndpointFilterCustomizationBean {

    @Autowired
    @Qualifier("authsr_AuthorizationServerSecurityFilterChain")
    private SecurityFilterChain authorizationServerSecurityFilterChain;

    @Autowired
    private RequestLocaleProvider requestLocaleProvider;

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
            if (filter instanceof OAuth2TokenEndpointFilter tef) {
                return new OAuth2TokenEndpointFilterWrapper(tef, requestLocaleProvider);
            } else {
                return filter;
            }
        });
    }
}