/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.sys;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jmix.core.annotation.Internal;
import org.dom4j.Document;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Cache that stores XML view definitions as string keys and their parsed DOM {@link Document} objects as values.
 */
@Internal
@Component("flowui_ViewXmlDocumentCache")
public class ViewXmlDocumentCache {

    protected Cache<String, Document> cache;

    public ViewXmlDocumentCache() {
        this(100);
    }

    protected ViewXmlDocumentCache(int cacheDescriptorsCount) {
        cache = CacheBuilder.newBuilder().maximumSize(cacheDescriptorsCount).build();
    }

    /**
     * Adds an XML view definition as a key and its associated parsed DOM {@link Document} as a value to the cache.
     *
     * @param xml      the XML view definition as a string key
     * @param document the parsed DOM {@link Document} object to be associated with the specified XML
     */
    public void put(String xml, Document document) {
        cache.put(xml, document);
    }

    /**
     * Retrieves the {@link Document} for the given XML string from the cache if it exists.
     *
     * @param xml the XML string for which the {@link Document} is to be retrieved
     * @return the cached {@link Document} associated with the given XML string,
     * or {@code null} if no cached value is present.
     */
    @Nullable
    public Document get(String xml) {
        return cache.getIfPresent(xml);
    }

    /**
     * Invalidates all entries in the cache.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }
}