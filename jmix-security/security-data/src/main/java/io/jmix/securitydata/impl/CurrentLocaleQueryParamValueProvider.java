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
import io.jmix.core.QueryParamValueProvider;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Takes query parameter values from the locale of the current authentication.
 * <p>
 * Parameter name must be {@code current_locale}
 */
@Component("sec_CurrentLocaleQueryParamValueProvider")
@Order(JmixOrder.LOWEST_PRECEDENCE - 300)
public class CurrentLocaleQueryParamValueProvider implements QueryParamValueProvider {

    public static final String CURRENT_LOCALE_PARAM = "current_locale";

    protected final CurrentAuthentication currentAuthentication;

    public CurrentLocaleQueryParamValueProvider(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    public boolean supports(String paramName) {
        return CURRENT_LOCALE_PARAM.equals(paramName);
    }

    @Nullable
    @Override
    public Object getValue(String paramName) {
        return currentAuthentication.getLocale().toString();
    }
}
