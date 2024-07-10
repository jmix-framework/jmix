/*
 * Copyright 2024 Haulmont.
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

package io.jmix.quartz.service;

import io.jmix.core.CacheOperations;
import io.jmix.core.common.util.Preconditions;
import jakarta.annotation.PostConstruct;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Currently running jobs store
 */
@Component("quartz_RunningJobsCache")
public class RunningJobsCache {

    private static final Logger log = LoggerFactory.getLogger(RunningJobsCache.class);

    protected Cache jobDetails;

    @Autowired
    protected CacheManager cacheManager;

    @Autowired
    protected CacheOperations cacheOperations;

    public static final String CACHE_NAME = "jmix-quartz-running-jobs-cache";

    @PostConstruct
    protected void init() {
        jobDetails = cacheManager.getCache(CACHE_NAME);
        if (jobDetails == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", CACHE_NAME));
        }
    }

    /**
     * Checks if job has at least one running trigger
     *
     * @param jobKey Job key
     * @return true if there is running trigger, false otherwise.
     */
    public boolean isJobRunning(JobKey jobKey) {
        return !getRunningTriggers(jobKey).isEmpty();
    }

    public Set<TriggerKey> getRunningTriggers(JobKey key) {
        RunningTriggersWrapper triggersWrapper = jobDetails.get(key, RunningTriggersWrapper.class);
        if (triggersWrapper == null) {
            return Collections.emptySet();
        } else {
            return triggersWrapper.getTriggerKeys();
        }
    }

    public void put(JobKey jobKey, TriggerKey triggerKey) {
        RunningTriggersWrapper triggersWrapper = jobDetails.get(jobKey, RunningTriggersWrapper.class);
        if (triggersWrapper == null) {
            triggersWrapper = new RunningTriggersWrapper();
        }
        triggersWrapper.addTrigger(triggerKey);

        jobDetails.put(jobKey, triggersWrapper);
    }

    public void invalidate(JobKey jobKey, TriggerKey triggerKey) {
        RunningTriggersWrapper triggersWrapper = jobDetails.get(jobKey, RunningTriggersWrapper.class);
        if (triggersWrapper != null) {
            triggersWrapper.removeTrigger(triggerKey);
            if (triggersWrapper.getTriggerKeys().isEmpty()) {
                // No more running triggers - evict entire wrapper
                jobDetails.evictIfPresent(jobKey);
            } else {
                jobDetails.put(jobKey, triggersWrapper);
            }
        }
    }

    /**
     * Remove all running triggers for provided job from cache
     *
     * @param jobKey Job key
     */
    public void invalidate(JobKey jobKey) {
        jobDetails.evictIfPresent(jobKey);
    }

    /**
     * Invalidate entire cache
     */
    public void invalidateAll() {
        log.info("Invalidate running Quartz jobs cache");
        jobDetails.invalidate();
    }

    private static class RunningTriggersWrapper implements Serializable {

        @Serial
        private static final long serialVersionUID = 7618135979394112959L;

        private final Set<TriggerKey> triggersKeys;

        public RunningTriggersWrapper() {
            triggersKeys = ConcurrentHashMap.newKeySet();
        }

        public Set<TriggerKey> getTriggerKeys() {
            return new HashSet<>(triggersKeys);
        }

        public void addTrigger(TriggerKey key) {
            Preconditions.checkNotNullArgument(key);
            triggersKeys.add(key);
        }

        public boolean removeTrigger(TriggerKey key) {
            Preconditions.checkNotNullArgument(key);
            return triggersKeys.remove(key);
        }
    }
}