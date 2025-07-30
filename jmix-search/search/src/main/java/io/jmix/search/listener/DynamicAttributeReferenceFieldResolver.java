/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.listener;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class DynamicAttributeReferenceFieldResolver {

    public static final String COMPOSITE_KEYS_ARE_NOT_SUPPORTED_MESSAGE = "Composite keys are not supported in the dynamic attributes. The entity type is %s.";

    private static final Map<Class<?>, String> typesMap = Map.of(
            UUID.class, "entityId",
            String.class, "stringEntityId",
            Integer.class, "intEntityId",
            Long.class, "longEntityId");

    private final MetadataTools metadataTools;

    public DynamicAttributeReferenceFieldResolver(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    public String getFieldName(MetaClass metaClass) {
        if (metadataTools.hasCompositePrimaryKey(metaClass)) {
            throw new IllegalStateException(String.format(COMPOSITE_KEYS_ARE_NOT_SUPPORTED_MESSAGE, metaClass.getName()));
        }
        Class<?> javaType = metadataTools.getPrimaryKeyProperty(metaClass).getJavaType();
        return typesMap.get(javaType);
    }
}
