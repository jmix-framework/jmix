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

import com.haulmont.cuba.core.model.common.User;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.core.security.SystemAuthenticationToken;

import java.io.Serializable;
import java.util.ArrayList;

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
        User user = new User();
        user.setLogin("test_admin");
        user.setLoginLowerCase("test_admin");
        SystemAuthenticationToken authentication = new SystemAuthenticationToken(user, new ArrayList<>());
        SecurityContextHelper.setAuthentication(authentication);
    }
}