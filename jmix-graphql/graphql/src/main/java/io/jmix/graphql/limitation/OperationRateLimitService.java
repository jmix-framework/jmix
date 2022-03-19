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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import graphql.execution.AbortExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component(value = OperationRateLimitService.NAME)
public class OperationRateLimitService {
    public static final String NAME = "gql_OperationRateLimitService";

    private final LoadingCache<String, Integer> attemptsCache;
    private LimitationProperties properties;

    @Autowired
    public void setLimitationProperties(LimitationProperties properties) {
        this.properties = properties;
    }

    public boolean isRateLimited() {
        return properties.getOperationRateLimitPerMinute() > 0;
    }

    public OperationRateLimitService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().
                expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<String, Integer>() {
            public Integer load(String key) {
                return 0;
            }
        });
    }

    public boolean isBlocked(String key) {
        try {
            return attemptsCache.get(key) >= properties.getOperationRateLimitPerMinute();
        } catch (ExecutionException e) {
            return false;
        }
    }

    public void queryPerformed(@Nullable String ip) {
        if (ip == null) {
            throw new AbortExecutionException("Can't get remote ip address");
        }
        if (isBlocked(ip)) {
            throw new AbortExecutionException("Exceeded the number of allowed requests per minute");
        }

        int attempts;
        try {
            attempts = attemptsCache.get(ip);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(ip, attempts);
    }
}
