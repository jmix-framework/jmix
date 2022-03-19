/*
 * Copyright 2019 Haulmont.
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

package io.jmix.eclipselink.impl.entitycache;

import com.google.common.collect.Sets;
import io.jmix.core.CacheOperations;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component("eclipselink_QueryCache")
public class StandardQueryCache implements QueryCache {

    protected Cache queries;

    @Autowired
    protected CacheManager cacheManager;
    @Autowired
    protected CacheOperations cacheOperations;

    public static final String QUERY_CACHE_NAME = "jmix-eclipselink-query-cache";

    protected static final Logger log = LoggerFactory.getLogger(QueryCache.class);

    @PostConstruct
    protected void init() {
        queries = cacheManager.getCache(QUERY_CACHE_NAME);
        if (queries == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", QUERY_CACHE_NAME));
        }
    }

    @Override
    public QueryResult get(QueryKey queryKey) {
        return queries.get(queryKey, QueryResult.class);
    }

    @Override
    public void put(QueryKey queryKey, QueryResult queryResult) {
        queries.put(queryKey, queryResult);
    }

    @Override
    public void invalidate(String typeName) {
        log.debug("Invalidate cache for type {}", typeName);
        invalidateByTypes(Sets.newHashSet(typeName));
    }

    @Override
    public void invalidate(Set<String> typeNames) {
        log.debug("Invalidate cache for types {}", typeNames);
        invalidateByTypes(typeNames);
    }

    protected void invalidateByTypes(Set<String> typeNames) {
        if (cacheOperations.isIterableCache(queries)) {
            Set<QueryKey> evicted = new HashSet<>();

            cacheOperations.<QueryKey, QueryResult>forEach(queries, (queryKey, queryResult) -> {
                if (CollectionUtils.containsAny(queryResult.getRelatedTypes(), typeNames)) {
                    evicted.add(queryKey);
                }
            });

            for (QueryKey queryKey : evicted) {
                queries.evictIfPresent(queryKey);

            }
        } else {
            queries.invalidate();
        }
    }

    @Override
    public void invalidateAll() {
        log.debug("Invalidate all cache");
        queries.invalidate();
    }

    @Override
    public long size() {
        if (cacheOperations.isIterableCache(queries)) {
            AtomicLong count = new AtomicLong();
            cacheOperations.forEach(queries, (queryKey, queryResult) -> count.incrementAndGet());
            return count.get();
        } else {
            return 0;
        }
    }
}
