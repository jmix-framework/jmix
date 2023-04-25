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

package io.jmix.data.impl;

import io.jmix.core.JmixOrder;
import io.jmix.core.session.SessionData;
import io.jmix.data.QueryParamValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

/**
 * Takes query parameter values from current session attributes set using {@link SessionData}.
 * <p>
 * Parameter names must be in the form {@code session_ATTRIBUTE} where {@code ATTRIBUTE} is the name of a session attribute.
 */
@Component("data_SessionQueryParamValueProvider")
@Order(JmixOrder.HIGHEST_PRECEDENCE - 100)
public class SessionQueryParamValueProvider implements QueryParamValueProvider {

    public static final String SESSION_PREFIX = "session_";

    private static final Logger log = LoggerFactory.getLogger(SessionQueryParamValueProvider.class);

    @Autowired
    private ObjectProvider<SessionData> sessionDataProvider;

    @Override
    public boolean supports(String paramName) {
        return paramName.startsWith(SESSION_PREFIX);
    }

    @Nullable
    @Override
    public Object getValue(String paramName) {
        if (supports(paramName)) {
            String attrName = paramName.substring(SESSION_PREFIX.length());
            try {
                return sessionDataProvider.getObject().getAttribute(attrName);
            } catch (Exception e) {
                log.warn("Unable to get session attribute {}: {}", paramName, e.toString());
            }
        }
        return null;
    }
}
