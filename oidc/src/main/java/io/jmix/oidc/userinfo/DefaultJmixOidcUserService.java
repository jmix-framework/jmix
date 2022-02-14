package io.jmix.oidc.userinfo;

import io.jmix.oidc.usermapper.OidcUserMapper;
import io.jmix.oidc.user.JmixOidcUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * A {@link OidcUserService} that is enabled by auto-configuration. In most cases you don't need to modify this class,
 * all behaviour may be defined in {@link OidcUserMapper}.
 * <p>
 * <p>
 * Class delegates the loading of the {@link OidcUser} to the {@link OidcUserService} from Spring Security and then
 * maps the user authorities to Jmix user authorities and creates a proper instance of {@code OidcUser} that may be used
 * by the framework. The returned user must implement the {@link JmixOidcUser} interface.
 * <p>
 * The transformation from {@code OidcUser} to {@link JmixOidcUser} is delegated to the {@link OidcUserMapper}.
 *
 * @see JmixOidcUser
 * @see org.springframework.security.oauth2.client.userinfo.OAuth2UserService
 * @see OidcUserService
 * @see OidcUserMapper
 */
public class DefaultJmixOidcUserService extends OidcUserService implements JmixOidcUserService {

    protected OidcUserMapper<? extends JmixOidcUser> userMapper;

    public DefaultJmixOidcUserService(OidcUserMapper<? extends JmixOidcUser> userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);
        return obtainJmixOidcUser(oidcUser);
    }

    /**
     * Obtains an instance of {@link JmixOidcUser} using the {@code OidcUser}. The instance of {@link JmixOidcUser} can
     * be either created or obtained from the user repository.
     *
     * @param oidcUser
     * @return
     */
    protected JmixOidcUser obtainJmixOidcUser(OidcUser oidcUser) {
        return userMapper.toJmixUser(oidcUser);
    }
}