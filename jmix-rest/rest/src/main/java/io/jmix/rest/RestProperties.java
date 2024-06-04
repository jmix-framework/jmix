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

package io.jmix.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.rest")
public class RestProperties {

    /**
     * Whether the passed entities versions should be validated before entities are persisted.
     */
    private final boolean optimisticLockingEnabled;

    /**
     * Whether "responseView" param is required.
     */
    private final boolean responseFetchPlanEnabled;
    private final int defaultMaxFetchSize;
    private final Map<String, Integer> entityMaxFetchSize;

    public RestProperties(
            @DefaultValue("false") boolean optimisticLockingEnabled,
            @DefaultValue("true") boolean responseFetchPlanEnabled,
            @DefaultValue("10000") int defaultMaxFetchSize,
            @Nullable Map<String, Integer> entityMaxFetchSize) {
        this.optimisticLockingEnabled = optimisticLockingEnabled;
        this.responseFetchPlanEnabled = responseFetchPlanEnabled;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
    }

    /**
     * @see #optimisticLockingEnabled
     */
    public boolean isOptimisticLockingEnabled() {
        return optimisticLockingEnabled;
    }

    /**
     * @see #responseFetchPlanEnabled
     */
    public boolean isResponseFetchPlanEnabled() {
        return responseFetchPlanEnabled;
    }

    public int getEntityMaxFetchSize(String entityName) {
        return entityMaxFetchSize.getOrDefault(entityName, defaultMaxFetchSize);
    }
}
