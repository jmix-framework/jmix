package ${project_rootPackage}.security;

import io.jmix.core.security.user.DefaultServiceUserProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * The class is responsible for initialization of system and anonymous users.
 */
@Component
public class AppServiceUserProvider extends DefaultServiceUserProvider {

    @Override
    protected Collection<GrantedAuthority> getSystemUserAuthorities() {
        return Collections.emptyList();
    }

    @Override
    protected Collection<GrantedAuthority> getAnonymousUserAuthorities() {
        return Collections.emptyList();
    }
}