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

package io.jmix.quartzflowui.cache;

import io.jmix.quartz.model.JobModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("quartz_JobModelCache")
public class JobModelCache {

    protected Cache jobModels;

    @Autowired
    protected CacheManager cacheManager;

    public static final String QUERY_CACHE_NAME = "jmix-quartz-jobmodel-cache";

    @EventListener(ApplicationStartedEvent.class)
    protected void init() {
        jobModels = cacheManager.getCache(QUERY_CACHE_NAME);
        if (jobModels == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", QUERY_CACHE_NAME));
        }
    }

    public List<JobModel> get(String key) {
        //noinspection unchecked
        return (List<JobModel>) jobModels.get(key, List.class);
    }

    public void put(String key, List<JobModel> jobs) {
        jobModels.put(key, jobs);
    }

    public void invalidate(String key) {
        jobModels.evictIfPresent(key);
    }

}
