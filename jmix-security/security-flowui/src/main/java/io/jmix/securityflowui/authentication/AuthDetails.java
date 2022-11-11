/*
 * Copyright 2020 Haulmont.
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

package io.jmix.securityflowui.authentication;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class contains authentication information.
 * <p>
 * Example usage:
 * <pre>
 * &#64;Autowired
 * private LoginViewSupport authenticator;
 *
 * private void doLogin(String username, String password) {
 *     loginViewSupport.authenticate(
 *         AuthDetails.of(event.getUsername(), event.getPassword())
 *     );
 * }
 * </pre>
 *
 * @see LoginViewSupport
 */
public class AuthDetails {

    protected String username;
    protected String password;

    protected Locale locale;
    protected TimeZone timeZone;
    protected boolean rememberMe = false;

    private AuthDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Creates a new instance with authentication credentials.
     *
     * @param username user login
     * @param password user password
     * @return new instance
     */
    public static AuthDetails of(String username, String password) {
        return new AuthDetails(username, password);
    }

    /**
     * @return user login
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return user password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return {@code true} if rememberMe is enabled, {@code false} otherwise
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * Sets rememberMe enabled. If rememberMe is enabled, the user can pass authentication without entering
     * credentials.
     *
     * @param rememberMe whether rememberMe should be used
     * @return current instance
     */
    public AuthDetails withRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
        return this;
    }

    /**
     * @return a locale or {@code null} if not set
     */
    @Nullable
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets user locale to be used in application.
     *
     * @param locale a locale
     * @return current instance
     */
    public AuthDetails withLocale(@Nullable Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * @return a timezone or {@code null} if not set
     */
    @Nullable
    public TimeZone getTimeZone() {
        return timeZone;
    }

    /**
     * Sets user timezone to be used in application.
     *
     * @param timeZone a timezone
     * @return current instance
     */
    public AuthDetails withTimeZone(@Nullable TimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }
}
