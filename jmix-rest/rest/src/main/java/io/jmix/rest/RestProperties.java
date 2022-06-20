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

import io.jmix.core.CorsProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.rest")
@ConstructorBinding
public class RestProperties {

    /**
     * Allowed origins for cross-domain requests.
     * @deprecated use {@link CorsProperties#getAllowedOrigins()}
     */
    @Deprecated
    private final String[] allowedOrigins;

    /**
     * Maximum size of the file that may be uploaded with REST API in bytes.
     */
    private final int maxUploadSize;

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
            @DefaultValue("*") String[] allowedOrigins,
            //todo DataSize type
            @DefaultValue("20971520") int maxUploadSize,
            @DefaultValue("false") boolean optimisticLockingEnabled,
            @DefaultValue("true") boolean responseFetchPlanEnabled,
            @DefaultValue("10000") int defaultMaxFetchSize,
            @Nullable Map<String, Integer> entityMaxFetchSize) {
        this.allowedOrigins = allowedOrigins;
        this.maxUploadSize = maxUploadSize;
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

    /**
     * @see #allowedOrigins
     *
     * @deprecated use {@link CorsProperties#getAllowedOrigins()}
     */
    @Deprecated
    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * @see #maxUploadSize
     */
    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    public int getEntityMaxFetchSize(String entityName) {
        return entityMaxFetchSize.getOrDefault(entityName, defaultMaxFetchSize);
    }
}
