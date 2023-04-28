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

package io.jmix.core.event;

import io.jmix.core.Id;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.utils.ObjectPathUtils;

import jakarta.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An object describing changes in entity attributes.
 *
 * @see EntityChangedEvent#getChanges()
 */
public class AttributeChanges {

    private final Set<Change> changes;
    private final Map<String, AttributeChanges> embeddedChanges;

    public static class Builder {
        private Set<Change> changes;
        private Map<String, Builder> embeddedChanges;

        private Builder() {
        }

        public static Builder create() {
            Builder builder = new Builder();
            builder.changes = new HashSet<>();
            builder.embeddedChanges = new HashMap<>();
            return builder;
        }

        public static Builder ofChanges(AttributeChanges attributeChanges) {
            Builder builder = new Builder();
            builder.changes = new HashSet<>(attributeChanges.changes);

            builder.embeddedChanges = new HashMap<>();
            for (Map.Entry<String, AttributeChanges> entry : attributeChanges.embeddedChanges.entrySet()) {
                builder.embeddedChanges.put(entry.getKey(), ofChanges(entry.getValue()));
            }

            return builder;
        }

        public Builder withChange(String attribute, @Nullable Object value) {
            changes.add(new Change(attribute, value));
            return this;
        }

        public Builder withEmbedded(String attribute, Consumer<Builder> consumer) {
            Builder builder = embeddedChanges.computeIfAbsent(attribute, key -> create());
            consumer.accept(builder);
            return this;
        }

        public Builder withEmbedded(String attribute, @Nullable Builder builder) {
            if (builder == null) {
                embeddedChanges.remove(attribute);
            } else {
                embeddedChanges.put(attribute, builder);
            }
            return this;
        }

        public Builder mergeChanges(@Nullable AttributeChanges attributeChanges) {
            if (attributeChanges != null) {
                changes.addAll(attributeChanges.changes);
                for (Map.Entry<String, AttributeChanges> entry : attributeChanges.embeddedChanges.entrySet()) {
                    Builder builder = embeddedChanges.computeIfAbsent(entry.getKey(), key -> new Builder());
                    builder.mergeChanges(entry.getValue());
                }
            }
            return this;
        }

        public AttributeChanges build() {
            Map<String, AttributeChanges> embeddedResultChanges = embeddedChanges.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build()));
            return new AttributeChanges(new HashSet<>(changes), embeddedResultChanges);
        }
    }

    /**
     * INTERNAL.
     */
    @Internal
    private AttributeChanges(Set<Change> changes, Map<String, AttributeChanges> embeddedChanges) {
        this.changes = changes;
        this.embeddedChanges = embeddedChanges;
    }

    /**
     * Returns names of changed attributes for the root entity.
     */
    public Set<String> getOwnAttributes() {
        return changes.stream().map(change -> change.name).collect(Collectors.toSet());
    }

    /**
     * Returns names of changed attributes for the root entity and all its embedded entities (if any).
     * Embedded attributes are represented by dot-separated paths.
     */
    public Set<String> getAttributes() {
        Set<String> attributes = new HashSet<>();
        for (Change change : changes) {
            attributes.add(change.name);
        }

        for (Map.Entry<String, AttributeChanges> entry : embeddedChanges.entrySet()) {
            AttributeChanges nestedChanges = entry.getValue();
            for (String attribute : nestedChanges.getAttributes()) {
                attributes.add(String.format("%s.%s", entry.getKey(), attribute));
            }
        }

        return attributes;
    }

    /**
     * Returns true if an attribute with the given name is changed.
     * If the attribute is not changed or does not exist at all, returns false.
     */
    public boolean isChanged(String attributeName) {
        String[] paths = attributeName.split("\\.", 2);
        if (paths.length > 1) {
            if (embeddedChanges.containsKey(paths[0])) {
                return embeddedChanges.get(paths[0]).isChanged(paths[1]);
            }
        }
        for (Change change : changes) {
            if (change.name.equals(attributeName))
                return true;
        }
        return false;
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
        String[] properties = ObjectPathUtils.isSpecialPath(attributeName)
                ? new String[]{attributeName}
                : attributeName.split("\\.");

        if (properties.length == 1) {
            for (Change change : changes) {
                if (change.name.equals(attributeName))
                    return (T) change.oldValue;
            }
        } else {
            AttributeChanges nestedChanges = embeddedChanges.get(properties[0]);
            if (nestedChanges != null) {
                return nestedChanges.getOldValue(attributeName.substring(attributeName.indexOf(".") + 1));
            }
        }
        return null;
    }

    /**
     * Type-safe method of getting the old value of a reference attribute.
     *
     * @param attributeName reference attribute name
     * @return Id of the referenced object
     */
    @Nullable
    public <E> Id<E> getOldReferenceId(String attributeName) {
        return getOldValue(attributeName);
    }

    /**
     * Type-safe method of getting the old value of a collection attribute.
     * <p>
     * Usage example:
     * <pre>
     * Collection&lt;Id&lt;OrderLine, UUID&gt;&gt; orderLines = event.getChanges().getOldCollection("orderLines", OrderLine.class);
     * for (Id&lt;OrderLine&gt; orderLineId : orderLines) {
     *     OrderLine orderLine = dataManager.load(orderLineId).one();
     *     // ...
     * }
     * </pre>
     *
     * @param attributeName collection attribute name
     * @param entityClass   class of the attribute
     * @return collection of Ids
     */
    public <E> Collection<Id<E>> getOldCollection(String attributeName, Class<E> entityClass) {
        return getOldValue(attributeName);
    }

    /**
     * @return true if changes is not empty
     */
    public boolean hasChanges() {
        if (!changes.isEmpty())
            return true;
        for (AttributeChanges embedded : embeddedChanges.values()) {
            if (embedded.hasChanges())
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "AttributeChanges{"
                + getAttributes().stream()
                .map(name -> name + ": " + getOldValue(name))
                .collect(Collectors.joining(","))
                + '}';
    }

    /**
     * INTERNAL.
     * Contains name and old value of a changed attribute.
     */
    @Internal
    public static class Change {

        public final String name;
        public final Object oldValue;

        public Change(String name, @Nullable Object oldValue) {
            this.name = name;
            this.oldValue = oldValue;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Change that = (Change) o;

            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
