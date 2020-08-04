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

package io.jmix.security.constraint;


import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.UUID;

@Component(PredefinedQueryParameters.NAME)
public class PredefinedQueryParameters {
    public static final String NAME = "sec_PredefinedQueryParameters";

    public static final String PARAM_SESSION_ATTR = "session$";
    public static final String PARAM_USER_LOGIN = "userLogin";
    public static final String PARAM_USER_ID = "userId";

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    /**
     * Get a value of the query parameter provided by security authentication.
     *
     * @param paramName parameter to set in a query
     * @return parameter value
     */
    public @Nullable
    Object getParameterValue(String paramName) {
        if (paramName.startsWith(PARAM_SESSION_ATTR)) {
            String attrName = paramName.substring(PARAM_SESSION_ATTR.length());

            if (PARAM_SESSION_ATTR.equals(attrName)) {
                return currentAuthentication.getUser().getUsername();
            } else if (PARAM_USER_ID.equals(attrName)) {
                return UUID.fromString(currentAuthentication.getUser().getKey());
            }
        }
        return null;
    }
}
