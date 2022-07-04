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

package io.jmix.flowui.sys;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.dom4j.Document;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("flowui_ViewXmlDocumentCache")
public class ViewXmlDocumentCache {

    protected Cache<String, Document> cache;

    public ViewXmlDocumentCache() {
        this(100);
    }

    protected ViewXmlDocumentCache(int cacheDescriptorsCount) {
        cache = CacheBuilder.newBuilder().maximumSize(cacheDescriptorsCount).build();
    }

    public void put(String xml, Document document) {
        cache.put(xml, document);
    }

    @Nullable
    public Document get(String xml) {
        return cache.getIfPresent(xml);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }
}