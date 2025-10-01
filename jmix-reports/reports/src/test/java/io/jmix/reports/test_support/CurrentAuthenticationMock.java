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

package io.jmix.reports.test_support;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Facilities to mock security-related behavior in tests, e.g. to specify current locale or current user.
 * Implement missing methods if you need them.
 *
 * @see org.springframework.test.context.bean.override.convention.TestBean
 */
public class CurrentAuthenticationMock implements CurrentAuthentication {

    private Locale locale = Locale.ENGLISH;

    @Override
    public Authentication getAuthentication() {
        throw new UnsupportedOperationException();
    }

    @Override
    public UserDetails getUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public TimeZone getTimeZone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSet() {
        return true;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
