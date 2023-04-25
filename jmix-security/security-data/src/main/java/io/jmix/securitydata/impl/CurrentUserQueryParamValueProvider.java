/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securitydata.impl;

import io.jmix.core.JmixOrder;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.data.QueryParamValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Takes query parameter values from the currently authenticated (or substituted) user object.
 * <p>
 * Parameter names must be in the form {@code current_user_ATTRIBUTE} where {@code ATTRIBUTE} is the name of
 * an attribute of the user object.
 */
@Component("sec_CurrentUserQueryParamValueProvider")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 100)
public class CurrentUserQueryParamValueProvider implements QueryParamValueProvider {

    public static final String CURRENT_USER_PREFIX = "current_user_";

    @Autowired
    protected CurrentUserSubstitution currentUserSubstitution;

    @Override
    public boolean supports(String paramName) {
        return paramName.startsWith(CURRENT_USER_PREFIX);
    }

    @Nullable
    @Override
    public Object getValue(String paramName) {
        if (supports(paramName)) {
            String attrName = paramName.substring(CURRENT_USER_PREFIX.length());

            UserDetails user = currentUserSubstitution.getEffectiveUser();
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
