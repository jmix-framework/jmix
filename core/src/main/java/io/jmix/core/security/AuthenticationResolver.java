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

package io.jmix.core.security;

import org.springframework.security.core.Authentication;

/**
 * Strategy for accessing root authentication from wrapped authentication in the current security context.
 */
public interface AuthenticationResolver {
    /**
     * @return true if resolving strategy supports authentication from the current security context
     */
    boolean supports(Authentication authentication);

    /**
     * Resolve root authentication from the authentication
     */
    Authentication resolveAuthentication(Authentication authentication);
}
