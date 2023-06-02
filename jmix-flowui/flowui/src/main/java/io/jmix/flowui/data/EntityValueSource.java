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

public interface EntityValueSource<E, V> extends ValueSource<V>, EntityDataUnit {

    /**
     * @return entity
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
