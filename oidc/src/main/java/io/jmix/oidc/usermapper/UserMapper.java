package io.jmix.oidc.usermapper;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interface is used for transforming some "external" object containing information about user to the instance of the
 * user used by Jmix. Examples of "external" objects are LDAP DirContext, OpenID Connect OidcUser, etc.
 *
 * @param <S> type of the object that stores information about the user
 * @param <T> type of the user used by Jmix
 */
//todo move interface to jmix-security in order to reuse user mappers between different add-ons
//todo remove UserMapper from the add-on
public interface UserMapper<S, T extends UserDetails> {

    /**
     * Transforms an object with user information to the instance of the user used by Jmix. Method implementations may
     * also perform users synchronization, e.g. to store users in the database.
     *
     * @param userInfo the object that stores information about the user (may be gotten from LDAP, OpenID Connect
     *                 provider, etc.
     * @return an instance of Jmix user that may be set into security context
     */
    T toJmixUser(S userInfo);
}

