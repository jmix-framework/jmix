package ${project_rootPackage}.security;

import ${project_rootPackage}.entity.User;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.oidc.claimsmapper.ClaimsRolesMapper;
import io.jmix.oidc.usermapper.SynchronizingOidcUserMapper;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class OidcUserMapper extends SynchronizingOidcUserMapper<User>  {

    public OidcUserMapper(UnconstrainedDataManager dataManager,
                                UserRepository userRepository,
                                ClaimsRolesMapper claimsRolesMapper,
                                RoleGrantedAuthorityUtils roleGrantedAuthorityUtils) {
        super(dataManager, userRepository, claimsRolesMapper, roleGrantedAuthorityUtils);

        //store role assignments in the database (false by default)
        setSynchronizeRoleAssignments(false);
    }

    @Override
    protected Class<User> getApplicationUserClass() {
        return User.class;
    }

    @Override
    protected void populateUserAuthorities(OidcUser oidcUser, User jmixUser) {
        Collection<? extends GrantedAuthority> authorities = claimsRolesMapper.toGrantedAuthorities(oidcUser.getClaims());
        jmixUser.setAuthorities(authorities);
    }

    @Override
    protected void populateUserAttributes(OidcUser oidcUser, User jmixUser) {
        jmixUser.setUsername(oidcUser.getName());
        jmixUser.setFirstName(oidcUser.getGivenName());
        jmixUser.setLastName(oidcUser.getFamilyName());
        jmixUser.setEmail(oidcUser.getEmail());
    }
}
