/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.data;

import com.vaadin.flow.shared.Registration;
import io.jmix.core.metamodel.model.MetaPropertyPath;

import org.springframework.lang.Nullable;
import java.util.EventObject;
import java.util.function.Consumer;

/**
 * A specialized {@link ValueSource} that represents a data source tied to an entity instance
 * and its properties within a data-aware context.
 *
 * @param <E> the type of the entity tied to this value source
 * @param <V> the type of the value provided by this value source
 */
public interface EntityValueSource<E, V> extends ValueSource<V>, EntityDataUnit {

    /**
     * Returns the current item associated with the source.
     *
     * @return the current item if available, or null if not set
     */
    @Nullable
    E getItem();

    /**
     * @return property path
     */
    MetaPropertyPath getMetaPropertyPath();

    /**
     * @return true if data model security check is required on data binding
     */
    boolean isDataModelSecurityEnabled();

    /**
     * Registers a listener that will be notified when the related entity instance is changed.
     *
     * @param listener the listener to add
     * @return a {@link Registration} instance that allows the removal of the listener
     */
    Registration addInstanceChangeListener(Consumer<InstanceChangeEvent<E>> listener);

    /**
     * An event fired when related entity instance is changed.
     *
     * @param <E> entity type
     */
    class InstanceChangeEvent<E> extends EventObject {
        private final E prevItem;
        private final E item;

        public InstanceChangeEvent(EntityValueSource<E, ?> source, @Nullable E prevItem, @Nullable E item) {
            super(source);
            this.prevItem = prevItem;
            this.item = item;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EntityValueSource<E, ?> getSource() {
            return (EntityValueSource<E, ?>) super.getSource();
        }

        /**
         * @return current item
         */
        @Nullable
        public E getItem() {
            return item;
        }

        /**
         * @return previous selected item
         */
        @Nullable
        public E getPrevItem() {
            return prevItem;
        }
    }
}
