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

import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;

import org.springframework.lang.Nullable;
import java.util.Locale;

/**
 * A resolver that provides {@link Locale} if current authentication does not contain it.
 *
 * @see CurrentAuthentication
 */
public interface AuthenticationLocaleResolver extends Ordered {

    /**
     * @param authentication authentication to check
     * @return {@code true} if resolver supports given authentication
     */
    boolean supports(Authentication authentication);

    /**
     * @param authentication authentication
     * @return locale that should be used, or {@code null}
     */
    @Nullable
    Locale getLocale(Authentication authentication);
}
