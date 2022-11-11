/*
 * Copyright 2021 Haulmont.
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

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.AttributeChangesProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public abstract class BaseAttributeChangesProvider implements AttributeChangesProvider {
    protected EntityStates entityStates;
    protected Metadata metadata;
    protected MetadataTools metadataTools;

    @Autowired
    public void setEntityStates(EntityStates entityStates) {
        this.entityStates = entityStates;
    }

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Override
    public AttributeChanges getAttributeChanges(Object entity) {
        checkEntityState(entity);

        AttributeChanges.Builder builder = AttributeChanges.Builder.create();

        if (!entityStates.isManaged(entity)) {
            return builder.build();
        }

        if (entityStates.isNew(entity)) {
            for (MetaProperty property : metadata.getClass(entity).getProperties()) {
                if (metadataTools.isJpa(property)) {
                    builder.withChange(property.getName(), EntityValues.getValue(entity, property.getName()));
                }
            }
            return builder.build();
        }

        buildChangesByImplementation(builder, entity, this::convertValueIfNeeded);

        buildExtraChanges(builder, entity);

        return builder.build();
    }

    protected abstract void buildChangesByImplementation(AttributeChanges.Builder builder,
                                                         Object entity,
                                                         BiFunction<Object, MetaProperty, Object> transformer);

    @Override
    public Set<String> getChangedAttributeNames(Object entity) {
        checkEntityState(entity);

        if (!entityStates.isManaged(entity)) {
            return Collections.emptySet();
        }

        if (entityStates.isNew(entity)) {
            return metadata.getClass(entity).getProperties().stream()
                    .filter(property -> metadataTools.isJpa(property))
                    .map(MetadataObject::getName)
                    .collect(Collectors.toSet());
        }

        return getChangedAttributeNamesByImplementation(entity);
    }

    protected abstract Set<String> getChangedAttributeNamesByImplementation(Object entity);

    @Override
    public boolean isChanged(Object entity) {
        checkEntityState(entity);

        if (entityStates.isNew(entity)) {
            return true;
        }

        return !getChangedAttributeNames(entity).isEmpty();
    }

    @Override
    public boolean isChanged(Object entity, String... attributes) {
        Set<String> changedAttributeNames = getChangedAttributeNames(entity);
        for (String attribute : attributes) {
            if (changedAttributeNames.contains(attribute))
                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public Object getOldValue(Object entity, String attribute) {
        checkEntityState(entity);

        if (!entityStates.isManaged(entity) && !EntitySystemAccess.isEmbeddable(entity)) {
            throw new IllegalArgumentException(String.format("The entity %s is not in the Managed state", entity));
        }

        if (entityStates.isNew(entity)) {
            return null;
        }

        if (!isChanged(entity, attribute)) {
            return EntityValues.getValue(entity, attribute);
        }

        Object oldValue = getOldValueByImplementation(entity, attribute);
        MetaProperty metaProperty = metadata.getClass(entity).getProperty(attribute);
        return convertValueIfNeeded(oldValue, metaProperty);
    }

    @Nullable
    protected abstract Object getOldValueByImplementation(Object entity, String attribute);

    @Nullable
    protected Object convertValueIfNeeded(@Nullable Object value, MetaProperty metaProperty) {
        if (value != null) {
            Range range = metaProperty.getRange();
            if (range.isEnum()) {
                for (Object o : range.asEnumeration().getValues()) {
                    EnumClass<?> enumValue = (EnumClass<?>) o;
                    if (value.equals(enumValue.getId())) {
                        return enumValue;
                    }
                }
            } else if (range.isClass() && range.getCardinality().isMany()) {
                if (isSoftDeletionEnabled()) {
                    if (metadataTools.isSoftDeletable(range.asClass().getJavaClass())) {
                        return ((Collection<?>) value).stream()
                                .filter(item -> !EntityValues.isSoftDeleted(item)
                                        || isChanged(item, metadataTools.getDeletedDateProperty(item)))
                                .collect(Collectors.toCollection(() -> newCollectionForProperty(metaProperty)));
                    }
                }
            }
        }
        return value;
    }

    protected abstract boolean isSoftDeletionEnabled();

    protected void buildExtraChanges(AttributeChanges.Builder builder, Object entity) {
        if (entity instanceof Entity) {
            Collection<EntityEntryExtraState> extraStates = ((Entity) entity).__getEntityEntry().getAllExtraState();
            for (EntityEntryExtraState state : extraStates) {
                if (state instanceof EntityValuesProvider) {
                    for (AttributeChanges.Change change : ((EntityValuesProvider) state).getChanges()) {
                        builder.withChange(change.name, change.oldValue);
                    }
                }
            }
        }
    }

    protected Collection<Object> newCollectionForProperty(MetaProperty metaProperty) {
        if (List.class.isAssignableFrom(metaProperty.getJavaType())) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(metaProperty.getJavaType())) {
            return new LinkedHashSet<>();
        } else {
            throw new RuntimeException(String.format("Could not instantiate collection with class [%s].",
                    metaProperty.getJavaType()));
        }
    }

    protected void checkEntityState(Object entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        EntityPreconditions.checkEntityType(entity);
    }
}
