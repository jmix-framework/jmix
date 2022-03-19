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

package io.jmix.data.impl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public interface EntityAttributesEraser {

    ReferencesCollector collectErasingReferences(Collection entityList, Predicate predicate);

    void eraseReferences(EntityAttributesEraser.ReferencesCollector referencesCollector);

    void restoreAttributes(Object entity);

    class ReferencesCollector {
        protected Map<Object, ReferencesByEntity> references = new HashMap<>();

        protected static class ReferencesByEntity {
            protected final Multimap<String, Object> referencesByAttributes = HashMultimap.create();

            public Collection<String> getAttributes() {
                return referencesByAttributes.keySet();
            }

            public Collection<Object> getReferences(String attribute) {
                return referencesByAttributes.get(attribute);
            }

            public void addReference(String attribute, Object reference) {
                referencesByAttributes.put(attribute, reference);
            }
        }

        public Collection<Object> getEntities() {
            return references.keySet();
        }

        public Collection<String> getAttributes(Object entity) {
            ReferencesByEntity referencesByEntity = references.get(entity);
            return referencesByEntity == null ? Collections.emptyList() : referencesByEntity.getAttributes();
        }

        public Collection<Object> getReferencesByAttribute(Object entity, String attribute) {
            ReferencesByEntity referencesByEntity = references.get(entity);
            return referencesByEntity == null ? Collections.emptyList() : referencesByEntity.getReferences(attribute);
        }

        public void addReference(Object entity, Object reference, String propertyName) {
            ReferencesByEntity referencesByEntity = references.computeIfAbsent(entity, e -> new ReferencesByEntity());
            referencesByEntity.addReference(propertyName, reference);
        }
    }
}
