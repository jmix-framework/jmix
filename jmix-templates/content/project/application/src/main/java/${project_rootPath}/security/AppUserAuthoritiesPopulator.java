package ${project_rootPackage}.security;

import ${project_rootPackage}.entity.User;
import io.jmix.core.security.user.UserAuthoritiesPopulator;
import io.jmix.simplesecurity.SimpleSecurityProperties;
import io.jmix.simplesecurity.role.GrantedAuthorityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * The class is used by the {@link io.jmix.core.security.user.DatabaseUserRepository} to populate granted authorities
 * of the loaded user.
 */
@Component
public class AppUserAuthoritiesPopulator implements UserAuthoritiesPopulator<User> {

    private static final String USER_ROLE = "USER";

    private GrantedAuthorityUtils grantedAuthorityUtils;

    private SimpleSecurityProperties simpleSecurityProperties;

    public AppUserAuthoritiesPopulator(GrantedAuthorityUtils grantedAuthorityUtils,
                                       SimpleSecurityProperties simpleSecurityProperties) {
        this.grantedAuthorityUtils = grantedAuthorityUtils;
        this.simpleSecurityProperties = simpleSecurityProperties;
    }

    @Override
    public void populateUserAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        GrantedAuthority userRole = grantedAuthorityUtils.createRoleGrantedAuthority(USER_ROLE);
        authorities.add(userRole);
        if ("admin".equals(user.getUsername())) {
            GrantedAuthority adminRole = grantedAuthorityUtils.createRoleGrantedAuthority(simpleSecurityProperties.getAdminRole());
            authorities.add(adminRole);
        }
        user.setAuthorities(authorities);
    }
}