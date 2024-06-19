/*
 * Copyright 2024 Haulmont.
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

package io.jmix.securityresourceserver.requestmatcher.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

/**
 * Utility class for working with {@link RequestMatcher} required by resource server.
 */
public class RequestMatcherUtils {

    private static final RequestMatcher NEVER_REQUEST_MATCHER = new NeverRequestMatcher();

    /**
     * Creates the {@link OrRequestMatcher} from the given {@code requestMatchers} if the collection of matchers is not
     * empty. Otherwise, returns a request matcher that always returns false.
     */
    public static RequestMatcher createCombinedRequestMatcher(List<RequestMatcher> requestMatchers) {
        return requestMatchers.isEmpty() ? NEVER_REQUEST_MATCHER : new OrRequestMatcher(requestMatchers);
    }

    /**
     * {@link RequestMatcher} that always returns false.
     */
    private static class NeverRequestMatcher implements RequestMatcher {

        @Override
        public boolean matches(HttpServletRequest request) {
            return false;
        }
    }
}
