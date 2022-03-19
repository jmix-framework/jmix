package io.jmix.oidc.usermapper;

import io.jmix.oidc.user.HasOidcUserDelegate;
import io.jmix.oidc.user.JmixOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Abstract class implements the {@link OidcUserMapper} and may be used for creating your own mappers.
 *
 * @param <T> class of Jmix user
 */
public abstract class BaseOidcUserMapper<T extends JmixOidcUser> implements OidcUserMapper<T> {

    @Override
    public T toJmixUser(OidcUser oidcUser) {
        T jmixUser = initJmixUser(oidcUser);
        populateUserAttributes(oidcUser, jmixUser);
        populateUserAuthorities(oidcUser, jmixUser);
        performAdditionalModifications(oidcUser, jmixUser);
        return jmixUser;
    }

    /**
     * Method returns an instance of Jmix user, which may be either a new instance or an instance loaded from the user
     * repository. Attributes and authorities will lately be filled in other methods. The responsibility of the current
     * method is just to create or load an existing instance.
     *
     * @param oidcUser OpenID user
     * @return new Jmix user instance or Jmix user loaded from user repository
     */
    protected abstract T initJmixUser(OidcUser oidcUser);

    /**
     * Fills attributes of {@code jmixUser} based on information from the {@code oidcUser}
     *
     * @param oidcUser
     * @param jmixUser
     */
    protected abstract void populateUserAttributes(OidcUser oidcUser, T jmixUser);

    /**
     * Fills authorities of {@code jmixUser} based on information from the {@code oidcUser}
     *
     * @param oidcUser
     * @param jmixUser
     */
    protected abstract void populateUserAuthorities(OidcUser oidcUser, T jmixUser);

    /**
     * Performs additional modifications of Jmix user instance. Override this method in case you want to do some
     * additional attribute values computations or if you want to do some operations with Jmix user instance, e.g. to
     * store it in the database, like it is done in the {@link SynchronizingOidcUserMapper}
     *
     * @param oidcUser
     * @param jmixUser
     */
    protected void performAdditionalModifications(OidcUser oidcUser, T jmixUser) {
        if (jmixUser instanceof HasOidcUserDelegate) {
            ((HasOidcUserDelegate) jmixUser).setDelegate(oidcUser);
        }
    }
}
