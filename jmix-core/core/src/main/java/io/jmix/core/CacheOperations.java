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

package io.jmix.core;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

/**
 * Used for additional cache operation like iterations over cache entries.
 * Spring cache API doesn't support some operations over cache (e.g. iterations),
 * so the {@link CacheOperations} facade uses specific API of cache implementations.
 */
@Component("core_CacheOperations")
public class CacheOperations {

    /**
     * Iterations over cache entries are supported for JCache and default {@link ConcurrentMapCache}.
     * @param cache
     * @return true if supported
     */
    public boolean isIterableCache(Cache cache) {
        return cache.getNativeCache() instanceof javax.cache.Cache ||
                cache instanceof ConcurrentMapCache;
    }

    @SuppressWarnings("unchecked")
    /**
     * Iterations over cache entries are supported for JCache and default {@link ConcurrentMapCache}
     * Performs the given action for each entry in this cache until all entries have been processed
     * @param cache
     * @param consumer - action
     */
    public <K, V> void forEach(Cache cache, BiConsumer<K, V> consumer) {
        if (cache.getNativeCache() instanceof javax.cache.Cache) {
            javax.cache.Cache<K, V> nativeCache = (javax.cache.Cache<K, V>) cache.getNativeCache();
            Iterator<javax.cache.Cache.Entry<K, V>> iterator = nativeCache.iterator();

            while (iterator.hasNext()) {
                javax.cache.Cache.Entry<K, V> entry = iterator.next();
                if (entry != null && entry.getKey() != null) {
                    //noinspection unchecked
                    consumer.accept(entry.getKey(), entry.getValue());
                }
            }

        } else if (cache instanceof ConcurrentMapCache) {
            //noinspection unchecked
            ((ConcurrentMap<K, V>) cache.getNativeCache()).forEach(consumer);
        } else {
            throw new UnsupportedOperationException("Unsupported cache type:" + cache.getClass());
        }
    }

    /**
     * @param cache - cache
     * @return a set view of the keys contained in this cache
     */
    public <K> Set<K> getKeys(Cache cache) {
        Set<K> result = new LinkedHashSet<>();

        //noinspection unchecked
        forEach(cache, (key, value) -> result.add((K) key));

        return result;
    }

    /**
     * @param cache - cache
     * @return a collection of the values contained in this cache
     */
    public <V> Collection<V> getValues(Cache cache) {
        List<V> result = new ArrayList<>();

        forEach(cache, (key, value) -> {
            if (value != null) {
                //noinspection unchecked
                result.add((V) value);
            }
        });

        return result;
    }

    /**
     * Used as safe alternative for {@link org.springframework.cache.Cache#get(java.lang.Object, java.util.concurrent.Callable)}
     * <br>
     * <p></p>
     * <b>WARNING: do not use Cache#get(java.lang.Object, java.util.concurrent.Callable) with Hazelcast. </b>
     * It causes NotSerializableException because {@code org.springframework.cache.jcache.JCacheCache#ValueLoaderEntryProcessor}
     * is not serializable.
     *
     * @param cache       to get value from
     * @param key         to get value by
     * @param valueLoader to load value in case of absence in cache
     * @param <K>         key type
     * @param <V>         value type
     * @return cached value
     */
    @Nullable
    public <K, V> V get(Cache cache, K key, Callable<V> valueLoader) {
        Cache.ValueWrapper wrapper = cache.get(key);
        V value;
        if (wrapper == null) {
            try {
                value = valueLoader.call();
            } catch (Exception e) {
                throw new Cache.ValueRetrievalException(key, valueLoader, e);
            }
            cache.put(key, value);
        } else {
            value = (V) wrapper.get();
        }
        return value;
    }
}
