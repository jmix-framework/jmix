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

package io.jmix.data.event;

import io.jmix.core.AppBeans;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.EntityChangeType;
import io.jmix.data.impl.EntityAttributeChanges;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public class EntityChangingEvent<E extends Entity> extends ApplicationEvent implements ResolvableTypeProvider {

    private E entity;
    private EntityChangeType type;
    private EntityAttributeChanges changes;

    /**
     * INTERNAL.
     */
    public EntityChangingEvent(Object source, E entity, EntityChangeType type, EntityAttributeChanges changes) {
        super(source);
        Preconditions.checkNotNullArgument(entity, "entity is null");
        this.entity = entity;
        this.type = type;
        this.changes = changes;
    }

    /**
     * Returns the entity
     */
    public E getEntity() {
        return entity;
    }

    /**
     * Returns entity change type. See {@link EntityChangeType}.
     */
    public EntityChangeType getType() {
        return type;
    }

    /**
     * Returns an object describing changes in the entity attributes.
     * <p>
     * Returned object is null for {@code CREATE} and {@code DELETE} change type events
     * or is {@link EntityAttributeChanges} which contains changed attributes
     * for {@code UPDATE} change type event.
     */
    public EntityAttributeChanges getChanges() {
        return changes;
    }

    /**
     * INTERNAL.
     */
    @Override
    public ResolvableType getResolvableType() {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        ExtendedEntities extendedEntities = AppBeans.get(ExtendedEntities.NAME);
        MetaClass metaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(entity.getClass()));
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(metaClass.getJavaClass()));
    }

    @Override
    public String toString() {
        return "EntityChangingEvent{" +
                "entity=" + entity +
                ", type=" + type +
                ", changes=" + changes +
                '}';
    }
}
