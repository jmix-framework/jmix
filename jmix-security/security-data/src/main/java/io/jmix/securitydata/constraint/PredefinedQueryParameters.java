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


import io.jmix.data.impl.QueryParamValuesManager;
import io.jmix.data.impl.SessionQueryParamValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;

/**
 * @deprecated
 * The functionality of assigning predefined query parameters has been moved to {@link io.jmix.data.QueryParamValueProvider}s.
 */
@Deprecated
@Component("sec_PredefinedQueryParameters")
public class PredefinedQueryParameters {

    public static final String CURRENT_USER_PREFIX = SessionQueryParamValueProvider.SESSION_PREFIX;

    @Autowired
    private QueryParamValuesManager queryParamValuesManager;

    @Nullable
    public Object getParameterValue(String paramName) {
        return queryParamValuesManager.getValue(paramName);
    }
}
