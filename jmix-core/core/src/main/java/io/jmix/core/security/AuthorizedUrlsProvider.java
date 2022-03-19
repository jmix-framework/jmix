/*
 * Copyright 2021 Haulmont.
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

import java.util.Collection;

/**
 * Provider used when configuring HTTP web security for API endpoints (like REST, MVC controllers).
 * Specifies lists of authenticated and anonymous URLs.
 */
public interface AuthorizedUrlsProvider {
    /**
     * Returns URL patterns that are allowed to any authenticated user.
     */
    Collection<String> getAuthenticatedUrlPatterns();

    /**
     * Returns URL patterns that are allowed to anonymous user.
     */
    Collection<String> getAnonymousUrlPatterns();
}
