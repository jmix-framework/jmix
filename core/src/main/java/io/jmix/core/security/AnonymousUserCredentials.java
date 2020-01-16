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

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Credentials object for anonymous session.
 */
public class AnonymousUserCredentials extends AbstractClientCredentials {
    private static final long serialVersionUID = 3137392403475947L;

    protected Object principal;

    public AnonymousUserCredentials() {
    }

    @Override
    public String getUserIdentifier() {
        return "";
    }

    public AnonymousUserCredentials(Locale locale) {
        super(locale, Collections.emptyMap());
    }

    public AnonymousUserCredentials(Locale locale, Map<String, Object> params) {
        super(locale, params);
    }

    public AnonymousUserCredentials(Object principal, Locale locale, Map<String, Object> params) {
        super(locale, params);

        this.principal = principal;

        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String toString() {
        return "AnonymousUserCredentials{}";
    }

    @Override
    public String getName() {
        return "";
    }
}