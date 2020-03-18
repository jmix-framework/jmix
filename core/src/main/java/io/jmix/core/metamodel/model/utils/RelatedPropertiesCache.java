/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.core.metamodel.model.utils;

import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RelatedPropertiesCache {
    private final Map<String, Set<String>> propertiesMap = new HashMap<>();

    private static final Map<Class, RelatedPropertiesCache> propertiesCacheMap = new ConcurrentHashMap<>();

    public static RelatedPropertiesCache getOrCreate(Class clazz) {
        return propertiesCacheMap.computeIfAbsent(clazz, RelatedPropertiesCache::new);
    }

    protected RelatedPropertiesCache(Class clazz) {
        Objects.requireNonNull(clazz, "class is null");

        MetaClass metaClass = AppBeans.get(Metadata.class).getClass(clazz);
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (metaProperty.isReadOnly() && isNotPersistent(metaProperty)) {

                for (String property : getRelatedProperties(metaProperty)) {
                    Set<String> set = propertiesMap.computeIfAbsent(property, k -> new HashSet<>());
                    set.add(metaProperty.getName());
                }

            }

        }
    }

    @Nullable
    public Set<String> getRelatedReadOnlyProperties(String propertyName) {
        return propertiesMap.get(propertyName);
    }

    private boolean isNotPersistent(MetaProperty metaProperty) {
        return !isPersistent(metaProperty);
    }

    private boolean isPersistent(MetaProperty metaProperty) {
        return metaProperty.getStore().getDescriptor().isPersistent();
    }

    private List<String> getRelatedProperties(MetaProperty metaProperty) {
        String relatedProperties = (String) metaProperty.getAnnotations().get("relatedProperties");
        List<String> result = Collections.emptyList();
        if (relatedProperties != null) {
            result = Arrays.asList(relatedProperties.split(","));
        }
        return result;
    }
}
