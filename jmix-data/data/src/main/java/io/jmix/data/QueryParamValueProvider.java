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

package io.jmix.data;

import javax.annotation.Nullable;

/**
 * Interface to be implemented by beans that provide values for query parameters if they are not set explicitly.
 * <p>
 * For any given query parameter with an empty value, all existing {@code QueryParamValueProvider} beans are requested
 * until a provider supporting this parameter is found. You can use the {@code Order} annotation with
 * the {@code JmixOrder.HIGHEST_PRECEDENCE - 10} value to override providers of the framework.
 *
 * @see io.jmix.data.impl.SessionQueryParamValueProvider
 */
public interface QueryParamValueProvider {

    /**
     * Returns true if this provider supports the given parameter.
     */
    boolean supports(String paramName);

    /**
     * Returns a value for the given parameter.
     */
    @Nullable
    Object getValue(String paramName);
}
