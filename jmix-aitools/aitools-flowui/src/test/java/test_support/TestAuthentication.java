/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test_support;

import io.jmix.core.security.impl.SubstitutedUserAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.function.Supplier;

/**
 * Runs test code as a given user without a user repository.
 */
public final class TestAuthentication {

    private TestAuthentication() {
    }

    /**
     * Runs the action authenticated as the given user.
     *
     * @param username user to authenticate as
     * @param action   code to run
     * @param <T>      result type
     * @return what the action returns
     */
    public static <T> T callAs(String username, Supplier<T> action) {
        return callWith(authentication(username), action);
    }

    /**
     * Runs the action authenticated as the given user.
     *
     * @param username user to authenticate as
     * @param action   code to run
     */
    public static void runAs(String username, Runnable action) {
        callAs(username, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Runs the action authenticated as one user who substitutes another.
     *
     * @param authenticated user who is logged in
     * @param substituted   user on whose behalf the logged-in user works
     * @param action        code to run
     * @param <T>           result type
     * @return what the action returns
     */
    public static <T> T callAsSubstituting(String authenticated, String substituted, Supplier<T> action) {
        return callWith(new SubstitutedUserAuthenticationToken(authentication(authenticated), user(substituted),
                List.of()), action);
    }

    private static <T> T callWith(Authentication authentication, Supplier<T> action) {
        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            return action.get();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }

    private static Authentication authentication(String username) {
        return new UsernamePasswordAuthenticationToken(user(username), null, List.of());
    }

    private static UserDetails user(String username) {
        return User.withUsername(username).password("").authorities(List.of()).build();
    }
}
