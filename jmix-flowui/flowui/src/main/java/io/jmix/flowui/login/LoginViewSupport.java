/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.login;

import io.jmix.core.CoreProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Interface that provides authentication via {@link AuthenticationManager}. It is intended to use from login view.
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
 * @see AuthDetails
 */
public interface LoginViewSupport {

    /**
     * Performs authentication via {@link AuthenticationManager} and uses
     * {@link UsernamePasswordAuthenticationToken} with credentials from {@link AuthDetails}.
     * <p>
     * If locale is not provided it will use the first locale from
     * {@link CoreProperties#getAvailableLocales()} list.
     * <p>
     * After successful authentication, there will be an attempt to open the main view.
     *
     * @param authDetails authentication details
     * @return a fully authenticated object including credentials
     * @throws AuthenticationException if exception occurs while authentication process
     */
    Authentication authenticate(AuthDetails authDetails) throws AuthenticationException;

}
