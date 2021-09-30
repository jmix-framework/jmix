/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.limitation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.graphql")
@ConstructorBinding
public class LimitationProperties {

    /**
     * Max allowed depth for query. Default value 0 means the client has no limit.
     */
    int maxQueryDepth;

    /**
     * Number of rate-limit per minute from one client IP address. Default value 0 means the client has no limit.
     */
    int operationRateLimitPerMinute;

    public LimitationProperties(@DefaultValue("0") int operationRateLimitPerMinute, @DefaultValue("0") int maxQueryDepth) {
        this.operationRateLimitPerMinute = operationRateLimitPerMinute;
        this.maxQueryDepth = maxQueryDepth;
    }

    /**
     * @see #operationRateLimitPerMinute
     */
    public int getOperationRateLimitPerMinute() {
        return operationRateLimitPerMinute;
    }

    /**
     * @see #maxQueryDepth
     */
    public int getMaxQueryDepth() {
        return maxQueryDepth;
    }
}
