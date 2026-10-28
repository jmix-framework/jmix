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

package io.jmix.authserver.authentication;

import io.jmix.authserver.event.AsUserAuthenticationSuccessEvent;
import io.jmix.core.annotation.Internal;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;

/**
 * Processes an authorization issued by the token endpoint before {@link AsUserAuthenticationSuccessEvent} is published
 * and the access token response is written. Implementations are Spring beans invoked for every grant type, in the
 * order of the beans.
 */
@Internal
@NullMarked
public interface IssuedAuthorizationProcessor {

    /**
     * @param authorization the saved authorization holding the issued tokens
     * @param request       the token request
     * @return the authorization to pass further; a processor that changes the authorization saves it and returns
     * the saved instance
     */
    OAuth2Authorization process(OAuth2Authorization authorization, HttpServletRequest request);
}
