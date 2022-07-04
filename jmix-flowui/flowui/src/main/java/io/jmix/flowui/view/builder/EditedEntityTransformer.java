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

package io.jmix.flowui.view.builder;

import com.vaadin.flow.component.HasValue;
import io.jmix.flowui.model.CollectionContainer;

/**
 * Interface to be implemented by beans that transform an entity after returning it from a detail view.
 * <p>
 * A collection of such beans is used by {@link DetailWindowBuilderProcessor}.
 */
public interface EditedEntityTransformer {

    /**
     * Transforms the entity to be added to the given container.
     *
     * @param editedEntity entity instance returned by detail view
     * @param container data container where the entity will be added
     * @return transformed instance
     */
    <E> E transformForCollectionContainer(E editedEntity, CollectionContainer<E> container);

    /**
     * Transforms the entity to be added to the given UI field.
     *
     * @param editedEntity entity instance returned by detail view
     * @param field UI field where the entity will be displayed
     * @return transformed instance
     */
    <E> E transformForField(E editedEntity, HasValue<?, E> field);
}
