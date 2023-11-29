package ${project_rootPackage}.security;

import ${project_rootPackage}.entity.User;
import io.jmix.core.security.user.UserAuthoritiesPopulator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The class is used by the {@link io.jmix.core.security.user.DatabaseUserRepository} to populate granted authorities
 * of the loaded user.
 */
@Component
public class AppUserAuthoritiesPopulator implements UserAuthoritiesPopulator<User> {
    private static final String ROLE_PREFIX = "ROLE_";

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_ROLE = "USER";

    @Override
    public void populateUserAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(createRoleAuthority(USER_ROLE));
        if ("admin".equals(user.getUsername())) {
            authorities.add(createRoleAuthority(ADMIN_ROLE));
        }
        user.setAuthorities(authorities);
    }

    private GrantedAuthority createRoleAuthority(String roleName) {
        return new SimpleGrantedAuthority(ROLE_PREFIX + roleName);
    }
}