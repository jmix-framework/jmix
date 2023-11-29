package ${project_rootPackage}.security;

import ${project_rootPackage}.entity.User;
import io.jmix.core.security.user.UserClassResolver;
import org.springframework.stereotype.Component;

/**
 * The class is used by the {@link io.jmix.core.security.user.DatabaseUserRepository} for resolving the class that
 * represents a user of the application.
 */
@Component
public class AppUserClassResolver implements UserClassResolver<User> {
    @Override
    public Class<User> getUserClass() {
        return User.class;
    }
}
