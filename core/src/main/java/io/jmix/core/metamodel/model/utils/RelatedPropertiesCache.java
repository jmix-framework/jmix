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

import com.google.common.base.Splitter;
import io.jmix.core.metamodel.annotation.ModelProperty;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
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
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(ModelProperty.class)) {
                String propertyName = StringUtils.uncapitalize(method.getName().substring(3));
                String[] related = method.getAnnotation(ModelProperty.class).related();
                List<String> relatedPropertyNames;
                if (related.length == 1) {
                    relatedPropertyNames = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(related[0]);
                } else {
                    relatedPropertyNames = Arrays.asList(related);
                }
                for (String relatedPropertyName : relatedPropertyNames) {
                    Set<String> set = propertiesMap.computeIfAbsent(relatedPropertyName, k -> new HashSet<>());
                    set.add(propertyName);
                }
            }
        }
    }

    @Nullable
    public Set<String> getRelatedReadOnlyProperties(String propertyName) {
        return propertiesMap.get(propertyName);
    }
}