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

package com.haulmont.cuba.core.app.events;

import com.haulmont.cuba.core.entity.contracts.Id;
import io.jmix.core.Entity;
import io.jmix.core.annotation.Internal;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An object describing changes in entity attributes.
 *
 * @see EntityChangedEvent#getChanges()
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.event.AttributeChanges}.
 */
@Deprecated
public class AttributeChanges {

    protected final io.jmix.core.event.AttributeChanges changes;

    /**
     * INTERNAL.
     */
    @Internal
    public AttributeChanges(io.jmix.core.event.AttributeChanges changes) {
        this.changes = changes;
    }

    /**
     * Returns names of changed attributes for the root entity.
     */
    public Set<String> getOwnAttributes() {
        return changes.getOwnAttributes();
    }

    /**
     * Returns names of changed attributes for the root entity and all its embedded entities (if any).
     * Embedded attributes are represented by dot-separated paths.
     */
    public Set<String> getAttributes() {
        return changes.getAttributes();
    }

    /**
     * Returns true if an attribute with the given name is changed.
     * If the attribute is not changed or does not exist at all, returns false.
     */
    public boolean isChanged(String attributeName) {
        return changes.isChanged(attributeName);
    }

    /**
     * Returns old value of a changed attribute with the given name. Old value can be null.
     * If the attribute is not changed or does not exist at all, returns null.
     * <p>
     * If the attribute is a reference to an entity, its old value is of type {@link Id}. If the attribute is a
     * collection of references, its old value is a collection of {@link Id}s.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getOldValue(String attributeName) {
        return changes.getOldValue(attributeName);
    }

    /**
     * Type-safe method of getting the old value of a reference attribute.
     *
     * @param attributeName reference attribute name
     * @return Id of the referenced object
     */
    @Nullable
    public <E extends Entity, K> Id<E, K> getOldReferenceId(String attributeName) {
        io.jmix.core.Id<E> oldReferenceId = changes.getOldReferenceId(attributeName);
        //noinspection unchecked
        return oldReferenceId == null ? null : Id.of((K)oldReferenceId.getValue(), oldReferenceId.getEntityClass());
    }

    /**
     * Type-safe method of getting the old value of a collection attribute.
     * <p>
     * Usage example:
     * <pre>
     * Collection&lt;Id&lt;OrderLine, UUID&gt;&gt; orderLines = event.getChanges().getOldCollection("orderLines", OrderLine.class);
     * for (Id&lt;OrderLine, UUID&gt; orderLineId : orderLines) {
     *     OrderLine orderLine = dataManager.load(orderLineId).one();
     *     // ...
     * }
     * </pre>
     *
     * @param attributeName collection attribute name
     * @param entityClass   class of the attribute
     * @return collection of Ids
     */
    public <E extends Entity, K> Collection<Id<E, K>> getOldCollection(String attributeName, Class<E> entityClass) {
        Collection<io.jmix.core.Id<E>> oldCollection = changes.getOldCollection(attributeName, entityClass);
        if (oldCollection != null) {
            //noinspection unchecked
            return oldCollection.stream()
                    .map(id -> Id.of((K)id.getValue(), id.getEntityClass()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String toString() {
        return changes.toString();
    }
}
