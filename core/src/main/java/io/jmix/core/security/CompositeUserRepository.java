package io.jmix.core.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An implementation of {@link UserRepository} that compose all {@link UserRepository}s
 * registered in an application and delegates operations to them.
 * <p>
 * For methods that return a single result, it returns the result of the first successful method execution
 * by the delegates (the first one that hasn't ended up with an exception).
 * Method returning a collection includes the results obtained from all the delegates.
 */
public class CompositeUserRepository implements UserRepository {

    @Autowired
    protected List<UserRepository> userRepositories;

    @Override
    public UserDetails getSystemUser() {
        for (UserRepository delegate : userRepositories) {
            UserDetails systemUser;
            try {
                systemUser = delegate.getSystemUser();
            } catch (Exception e) {
                continue;
            }
            return systemUser;
        }
        throw new UnsupportedOperationException("User repository does not provide the system user");
    }

    @Override
    public UserDetails getAnonymousUser() {
        for (UserRepository delegate : userRepositories) {
            UserDetails anonymousUser;
            try {
                anonymousUser = delegate.getAnonymousUser();
            } catch (Exception e) {
                continue;
            }
            return anonymousUser;
        }
        throw new UnsupportedOperationException("User repository does not provide the anonymous user");
    }

    @Override
    public List<? extends UserDetails> getByUsernameLike(String substring) {
        return userRepositories.stream()
                .flatMap(userRepository -> userRepository.getByUsernameLike(substring).stream())
                .collect(Collectors.toList());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        for (UserRepository delegate : userRepositories) {
            UserDetails userDetails;
            try {
                userDetails = delegate.loadUserByUsername(username);
            } catch (Exception e) {
                continue;
            }
            return userDetails;
        }
        throw new UsernameNotFoundException("User '" + username + "' not found");
    }
}
