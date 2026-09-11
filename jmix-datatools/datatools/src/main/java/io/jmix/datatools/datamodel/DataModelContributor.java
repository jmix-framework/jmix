/*
 * Copyright 2026 Haulmont.
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

package io.jmix.datatools.datamodel;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Describes entities and attributes that JPA metadata cannot describe, so that they take part in the
 * data model. Implementations are consulted in bean order. For {@link #describeEntity(MetaClass)} and
 * {@link #describeAttribute(MetaClass, MetaProperty)} the first one that handles an object wins; the
 * entities of {@link #getAdditionalEntities()} are collected from all implementations and
 * de-duplicated by entity name.
 * <p>
 * An implementation is expected not to throw: a call that fails is logged and skipped, and whatever
 * that implementation would have contributed is missing from the data model.
 */
@NullMarked
public interface DataModelContributor {

    /**
     * Returns entities to add to the data model in addition to JPA entities. The result is added to
     * what the other implementations return, so an entity already in the model is not added twice.
     *
     * @return contributed entity meta-classes, empty if none
     */
    default Collection<MetaClass> getAdditionalEntities() {
        return List.of();
    }

    /**
     * Describes the physical placement of an entity.
     *
     * @param metaClass entity meta-class
     * @return descriptor, or {@code null} if this contributor does not handle the entity
     */
    @Nullable
    default EntityDescriptor describeEntity(MetaClass metaClass) {
        return null;
    }

    /**
     * Describes a property that is not a JPA attribute.
     *
     * @param entity   owner entity meta-class
     * @param property property to describe
     * @return descriptor, or {@code null} if this contributor does not handle the property
     */
    @Nullable
    default AttributeDescriptor describeAttribute(MetaClass entity, MetaProperty property) {
        return null;
    }
}
