/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.impl.method;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Allows resolving the current authorized {@link UserDetails} as method argument.
 */
@Component("core_UserArgumentResolver")
public class UserArgumentResolver extends TypedArgumentResolver<UserDetails> {

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public UserArgumentResolver() {
        super(UserDetails.class);
    }

    @Override
    public UserDetails resolveArgument(MethodParameter parameter) {
        return currentAuthentication.isSet() ?
                currentAuthentication.getUser() :
                null;
    }
}
