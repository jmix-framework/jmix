package io.jmix.oidc.claimsmapper;

import io.jmix.oidc.usermapper.OidcUserMapper;
import io.jmix.oidc.user.OidcUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;

/**
 * Class delegates the loading of the {@link OidcUser} to the {@link OidcUserService} and then maps the user authorities
 * to Jmix user authorities and creates a proper instance of {@code OidcUser} that may be used by the framework. The
 * returned user must implement the {@link OidcUserDetails} interface.
 *
 * The transformation from {@code OidcUser} to {@link OidcUserDetails} is delegated to the {@link OidcUserMapper}.
 *
 * @see OidcUserDetails
 * @see org.springframework.security.oauth2.client.userinfo.OAuth2UserService
 * @see OidcUserService
 * @see OidcUserMapper
 */
public class DefaultJmixOidcUserService extends OidcUserService implements JmixOidcUserService {

    protected OidcUserMapper<? extends OidcUserDetails> userMapper;

    public DefaultJmixOidcUserService(OidcUserMapper<? extends OidcUserDetails> userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);
        return obtainJmixOidcUser(oidcUser);
    }

    /**
     * Obtains an instance of {@link OidcUserDetails} using the {@code OidcUser}. The instance of {@link OidcUserDetails} can
     * be either created or obtained from the user repository.
     *
     * @param oidcUser
     * @return
     */
    protected OidcUserDetails obtainJmixOidcUser(OidcUser oidcUser) {
        return userMapper.toJmixUser(oidcUser);
    }
}