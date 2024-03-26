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
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Currently running jobs store
 */
@Component("quartz_RunningJobsCache")
public class RunningJobsCache {

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

    @Nullable
    public JobDetail get(JobKey key) {
        return jobDetails.get(key, JobDetail.class);
    }

    public void put(JobKey key, JobDetail job) {
        jobDetails.put(key, job);
    }

    public void invalidate(JobKey key) {
        jobDetails.evictIfPresent(key);
    }
}