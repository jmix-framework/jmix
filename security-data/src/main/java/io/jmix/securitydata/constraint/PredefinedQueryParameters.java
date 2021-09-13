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

package io.jmix.securitydata.constraint;


import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Component("sec_PredefinedQueryParameters")
public class PredefinedQueryParameters {

    public static final String CURRENT_USER_PREFIX = "current_user_";

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    /**
     * Get a value of the query parameter provided by security authentication.
     *
     * @param paramName parameter to set in a query
     * @return parameter value
     */
     @Nullable
     public Object getParameterValue(String paramName) {
        if (paramName.startsWith(CURRENT_USER_PREFIX)) {
            String attrName = paramName.substring(CURRENT_USER_PREFIX.length());

            UserDetails user = currentAuthentication.getCurrentOrSubstitutedUser();
            BeanInfo info;
            try {
                info = Introspector.getBeanInfo(user.getClass());
                return Arrays.stream(info.getPropertyDescriptors())
                        .filter(pd -> pd.getName().equals(attrName))
                        .findAny()
                        .orElseThrow(() ->
                                new RuntimeException(String.format("Property %s not found in the user class", attrName)))
                        .getReadMethod()
                        .invoke(user);
            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(
                        String.format("Error getting %s property from the current user object", attrName), e);
            }
        }
        return null;
    }
}
