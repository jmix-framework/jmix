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
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.rest")
@ConstructorBinding
public class RestProperties {
    private final String[] allowedOrigins;
    private final int maxUploadSize;
    private final boolean optimisticLockingEnabled;
    private final boolean responseViewEnabled;
    private final int defaultMaxFetchSize;
    private final Map<String, Integer> entityMaxFetchSize;

    public RestProperties(
            @DefaultValue("*") String[] allowedOrigins,
            //todo DataSize type
            @DefaultValue("20971520") int maxUploadSize,
            @DefaultValue("false") boolean optimisticLockingEnabled,
            @DefaultValue("true") boolean responseViewEnabled,
            @DefaultValue("10000") int defaultMaxFetchSize,
            @Nullable Map<String, Integer> entityMaxFetchSize) {
        this.allowedOrigins = allowedOrigins;
        this.maxUploadSize = maxUploadSize;
        this.optimisticLockingEnabled = optimisticLockingEnabled;
        this.responseViewEnabled = responseViewEnabled;
        this.defaultMaxFetchSize = defaultMaxFetchSize;
        this.entityMaxFetchSize = entityMaxFetchSize == null ? Collections.emptyMap() : entityMaxFetchSize;
    }

    /**
     * @return whether the passed entities versions should be validated before entities are persisted
     */
    public boolean isOptimisticLockingEnabled() {
        return optimisticLockingEnabled;
    }

    /**
     * @return whether "responseView" param is required
     */
    public boolean isResponseViewEnabled() {
        return responseViewEnabled;
    }

    /**
     * @return allowed origins for cross-domain requests
     */
    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    /**
     * @return maximum size of the file that may be uploaded with REST API in bytes
     */
    public int getMaxUploadSize() {
        return maxUploadSize;
    }

    public int getEntityMaxFetchSize(String entityName) {
        return entityMaxFetchSize.getOrDefault(entityName, defaultMaxFetchSize);
    }
}
