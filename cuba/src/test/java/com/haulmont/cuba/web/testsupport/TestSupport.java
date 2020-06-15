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

package com.haulmont.cuba.web.testsupport;

import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.authentication.CoreAuthenticationToken;
import io.jmix.core.security.impl.CoreUser;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;

import static io.jmix.core.impl.StandardSerialization.deserialize;
import static io.jmix.core.impl.StandardSerialization.serialize;

public class TestSupport {

    @SuppressWarnings("unchecked")
    public static <T> T reserialize(Serializable object) {
        if (object == null) {
            return null;
        }

        return (T) deserialize(serialize(object));
    }

    public static void setAuthenticationToSecurityContext() {
        CoreUser user = new CoreUser("test_admin", "test_admin", "test_admin");
        CoreAuthenticationToken authentication = new CoreAuthenticationToken(user, Collections.emptyList());
        authentication.setLocale(Locale.US);
        SecurityContextHelper.setAuthentication(authentication);
    }
}