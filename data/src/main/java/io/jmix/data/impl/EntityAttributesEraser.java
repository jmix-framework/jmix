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
import io.jmix.core.JmixEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public interface EntityAttributesEraser {
    String NAME = "data_EntityAttributesEraser";

    ReferencesCollector collectErasingReferences(JmixEntity entity, Predicate<JmixEntity> predicate);

    ReferencesCollector collectErasingReferences(Collection<? extends JmixEntity> entityList, Predicate<JmixEntity> predicate);

    void eraseReferences(EntityAttributesEraser.ReferencesCollector referencesCollector);

    void restoreAttributes(JmixEntity entity);

    class ReferencesCollector {
        protected Map<JmixEntity, ReferencesByEntity> references = new HashMap<>();

        protected static class ReferencesByEntity {
            protected final Multimap<String, JmixEntity> referencesByAttributes = HashMultimap.create();

            public Collection<String> getAttributes() {
                return referencesByAttributes.keySet();
            }

            public Collection<JmixEntity> getReferences(String attribute) {
                return referencesByAttributes.get(attribute);
            }

            public void addReference(String attribute, JmixEntity reference) {
                referencesByAttributes.put(attribute, reference);
            }
        }

        public Collection<JmixEntity> getEntities() {
            return references.keySet();
        }

        public Collection<String> getAttributes(JmixEntity entity) {
            ReferencesByEntity referencesByEntity = references.get(entity);
            return referencesByEntity == null ? Collections.emptyList() : referencesByEntity.getAttributes();
        }

        public Collection<JmixEntity> getReferencesByAttribute(JmixEntity entity, String attribute) {
            ReferencesByEntity referencesByEntity = references.get(entity);
            return referencesByEntity == null ? Collections.emptyList() : referencesByEntity.getReferences(attribute);
        }

        public void addReference(JmixEntity entity, JmixEntity reference, String propertyName) {
            ReferencesByEntity referencesByEntity = references.computeIfAbsent(entity, e -> new ReferencesByEntity());
            referencesByEntity.addReference(propertyName, reference);
        }
    }
}
