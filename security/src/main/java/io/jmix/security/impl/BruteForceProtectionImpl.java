/*
 * Copyright (c) 2008-2016 Haulmont.
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

package io.jmix.security.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.jmix.security.BruteForceProtection;
import io.jmix.security.BruteForceProtectionProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Component("sec_BruteForceProtection")
public class BruteForceProtectionImpl implements BruteForceProtection {
    private LoadingCache<String, Integer> attemptsCache;
    @Autowired
    private BruteForceProtectionProperties properties;

    @PostConstruct
    protected void init() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(properties.getBlockInterval())
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(@Nonnull String key) throws Exception {
                        return 0;
                    }
                });
    }

    @Override
    public boolean isBlocked(String username, String ipAddress) {
        try {
            String cacheKey = makeCacheKey(username, ipAddress);
            Integer attemptsNumber = attemptsCache.get(cacheKey);
            return attemptsNumber >= properties.getMaxLoginAttemptsNumber();
        } catch (ExecutionException e) {
            throw new RuntimeException("BruteForce protection error", e);
        }
    }

    @Override
    public void registerLoginFailed(String username, String ipAddress) {
        try {
            String cacheKey = makeCacheKey(username, ipAddress);
            Integer attemptsNumber = attemptsCache.get(cacheKey);
            attemptsCache.put(cacheKey, attemptsNumber + 1);
        } catch (ExecutionException e) {
            throw new RuntimeException("BruteForce protection error", e);
        }
    }

    @Override
    public void registerLoginSucceeded(String username, String ipAddress) {
        attemptsCache.invalidate(makeCacheKey(username, ipAddress));
    }

    @Override
    public boolean isProtectionEnabled() {
        return properties.isEnabled();
    }

    private String makeCacheKey(String login, String ipAddress) {
        return login + "|" + ipAddress;
    }
}