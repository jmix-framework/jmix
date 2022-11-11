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

package com.haulmont.cuba.core.testsupport;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TestCurrentAuthentication implements CurrentAuthentication {

    private UserDetails user;

    public TestCurrentAuthentication() {
        user = User.builder()
                .username("test_admin")
                .password("test_admin")
                .authorities(Collections.emptyList())
                .build();
    }

    @Override
    public Authentication getAuthentication() {
        return new UsernamePasswordAuthenticationToken(user, "test_admin", Collections.emptyList());
    }

    @Override
    public UserDetails getUser() {
        return user;
    }

    @Override
    public UserDetails getUser(Map<String, Object> hints) {
        return user;
    }

    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone("UTC");
    }

    @Override
    public boolean isSet() {
        return true;
    }
}
