package ${project_rootPackage}.configuration;

import io.jmix.core.security.AuthorizedUrlsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

@Configuration
public class RestSecurityConfiguration {

    @Bean
    public AuthorizedUrlsProvider myAuthorizedUrlsProvider() {
        return new AuthorizedUrlsProvider() {
            @Override
            public Collection<String> getAuthenticatedUrlPatterns() {
                return List.of("/jmee/**");
            }

            @Override
            public Collection<String> getAnonymousUrlPatterns() {
                return List.of();
            }
        };
    }

}
