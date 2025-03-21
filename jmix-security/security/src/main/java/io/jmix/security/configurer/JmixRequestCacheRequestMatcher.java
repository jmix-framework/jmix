/*
 * Copyright 2025 Haulmont.
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

package io.jmix.security.configurer;

import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Represents additional request matcher that will be applied to RequestCache within VaadinDefaultRequestCache.
 * <p>
 * Create a Spring bean extends this class to register it.
 */
public abstract class JmixRequestCacheRequestMatcher implements RequestMatcher {
}
