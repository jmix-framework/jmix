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


import java.util.Set;

/**
 * Cache that contains queries and a collection of object identifiers
 */
public interface QueryCache {

    /**
     * Returns the query results associated with {@code queryKey} in this cache
     */
    QueryResult get(QueryKey queryKey);

    /**
     * Associates {@code queryResult} with {@code queryKey} in this cache
     */
    void put(QueryKey queryKey, QueryResult queryResult);

    /**
     * Discards cached query results for metaClass name {@code typeName}.
     */
    void invalidate(String typeName);

    /**
     * Discards cached query results for metaClass names {@code typeNames}.
     */
    void invalidate(Set<String> typeNames);

    /**
     * Discards all queries results in the cache.
     */
    void invalidateAll();

    /**
     * Returns number of entries in this cache.
     */
    long size();
}
