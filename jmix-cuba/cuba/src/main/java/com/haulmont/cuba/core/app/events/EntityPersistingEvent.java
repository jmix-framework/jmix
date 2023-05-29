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

package com.haulmont.cuba.core.app.events;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * A Spring application event that is sent before a new entity instance is persisted in the data store.
 * <p>
 * You can use {@code @EventListener} annotation on a bean method to handle this event. The handler works inside
 * the transaction which is going to persist the entity. The entity instance is in the NEW state.
 * <p>
 * Example of event handler:
 * <pre>
 * {@literal @}Component("test_OrderPersistingListener")
 * public class OrderPersistingListener {
 *
 *     {@literal @}EventListener
 *     protected void orderPersisting(EntityPersistingEvent&lt;Order&gt; event) {
 *         Order order = event.getEntity();
 *         //...
 *     }
 * }
 * </pre>
 *
 * @param <E> entity type
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.event.EntitySavingEvent}.
 */
@Deprecated
public class EntityPersistingEvent<E> extends ApplicationEvent implements ResolvableTypeProvider {

    private final E entity;
    private final MetaClass originalMetaClass;

    /**
     * INTERNAL.
     */
    @Internal
    public EntityPersistingEvent(Object source, E entity, MetaClass originalMetaClass) {
        super(source);
        Preconditions.checkNotNullArgument(entity, "entity is null");
        this.entity = entity;
        this.originalMetaClass = originalMetaClass;
    }

    /**
     * Returns entity to be persisted, in the NEW state.
     */
    public E getEntity() {
        return entity;
    }

    /**
     * INTERNAL.
     */
    @Internal
    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(originalMetaClass.getJavaClass()));
    }

    @Override
    public String toString() {
        return "EntityPersistingEvent{" +
                "entity=" + entity +
                '}';
    }
}
