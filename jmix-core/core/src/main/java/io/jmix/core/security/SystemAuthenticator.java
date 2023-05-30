/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.security;

import org.springframework.security.core.Authentication;

import org.springframework.lang.Nullable;

/**
 * Provides ad-hoc authentication, i.e. allows you to execute code on behalf of the 'system' or a specified user.
 * <p>
 * Example usage:
 * <pre>
 *     authenticator.runWithSystem(() -&gt; {
 *         // valid current thread's authentication presents here
 *     });
 * </pre>
 * or
 * <pre>
 *     authenticator.begin();
 *     try {
 *         // valid current thread's authentication presents here
 *     } finally {
 *         authenticator.end();
 *     }
 * </pre>
 *
 * @see #withSystem(AuthenticatedOperation)
 * @see #runWithSystem(Runnable)
 * @see #withUser(String, AuthenticatedOperation)
 * @see #runWithUser(String, Runnable)
 * @see #begin()
 * @see #begin(String)
 */
public interface SystemAuthenticator {

    /**
     * Begins an authenticated code block.
     * <br>
     * Saves the current thread session on a stack to get it back on {@link #end()}.
     * Subsequent {@link #end()} method must be called in "finally" section.
     *
     * @param login user login. If null, authenticates as the 'system' user.
     * @return populated {@code Authentication} object
     */
    Authentication begin(@Nullable String login);

    /**
     * Authenticate as the 'system' user.
     * <br>
     * Same as {@link #begin(String)} with null parameter.
     */
    Authentication begin();

    /**
     * End of an authenticated code block.
     * <br>
     * The previous session (or null) is set to security context.
     * Must be called in "finally" section of a try/finally block.
     */
    void end();

    /**
     * Execute code on behalf of the specified user and return a result.
     *
     * @param login     user login. If null, the system session is used.
     * @param operation code to execute
     * @return result of the execution
     */
    <T> T withUser(@Nullable String login, AuthenticatedOperation<T> operation);

    /**
     * Execute code on behalf of the specified user.
     *
     * @param login     user login. If null, the system session is used.
     * @param operation code to execute
     */
    void runWithUser(@Nullable String login, Runnable operation);

    /**
     * Execute code as the 'system' user and return a result.
     *
     * @param operation code to execute
     * @return result of the execution
     */
    <T> T withSystem(AuthenticatedOperation<T> operation);

    /**
     * Execute code as the 'system' user.
     *
     * @param operation code to execute
     */
    void runWithSystem(Runnable operation);

    /**
     * Operation with a result to be used in {@link #withSystem(AuthenticatedOperation)}
     * and {@link #withUser(String, AuthenticatedOperation)}.
     */
    interface AuthenticatedOperation<T> {
        @Nullable
        T call();
    }
}
