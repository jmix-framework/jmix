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

package io.jmix.ui.component;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.Nested;

import javax.annotation.Nullable;

/**
 * Interface for entity aware screen facets.
 *
 * @param <E> entity type
 * @see EditorScreenFacet
 * @see LookupScreenFacet
 */
public interface EntityAwareScreenFacet<E> {

    /**
     * Sets entity class.
     *
     * @param entityClass entity class
     */
    @StudioProperty(type = PropertyType.ENTITY_CLASS, typeParameter = "E")
    void setEntityClass(@Nullable Class<E> entityClass);

    /**
     * @return entity class
     */
    @Nullable
    Class<E> getEntityClass();

    /**
     * Sets list component.
     * <p>
     * The component is used to get the {@code container} if it is not set explicitly by
     * {@link #setContainer(CollectionContainer)} method.
     * <p>
     * Usually, the list component is a {@code Table} or {@code DataGrid} displaying the list of entities.
     */
    @StudioProperty(type = PropertyType.COMPONENT_REF, typeParameter = "E",
            options = "io.jmix.ui.component.ListComponent")
    void setListComponent(@Nullable ListComponent<E> listComponent);

    /**
     * @return list component
     */
    @Nullable
    ListComponent<E> getListComponent();

    /**
     * Sets the {@link EntityPicker} component.
     * <p>
     * If the field is set, the framework sets the committed entity to the field after successful editor commit.
     */
    @StudioProperty(name = "field", type = PropertyType.COMPONENT_REF, typeParameter = "E",
            options = "io.jmix.ui.component.EntityPicker")
    void setEntityPicker(@Nullable EntityPicker<E> entityPicker);

    /**
     * @return {@link EntityPicker}
     */
    @Nullable
    EntityPicker<E> getEntityPicker();

    /**
     * Sets {@link CollectionContainer}.
     * <p>
     * The container is updated after the screen is committed. If the container is {@link Nested},
     * the framework automatically initializes the reference to the parent entity and sets up data contexts
     * for editing compositions.
     */
    @StudioProperty(type = PropertyType.COLLECTION_DATACONTAINER_REF, typeParameter = "E")
    void setContainer(@Nullable CollectionContainer<E> container);

    /**
     * @return {@link CollectionContainer}
     */
    @Nullable
    CollectionContainer<E> getContainer();
}
