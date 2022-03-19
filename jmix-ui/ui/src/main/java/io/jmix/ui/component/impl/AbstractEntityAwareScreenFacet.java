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

package io.jmix.ui.component.impl;

import io.jmix.ui.component.EntityAwareScreenFacet;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;

import javax.annotation.Nullable;

public abstract class AbstractEntityAwareScreenFacet<E, S extends Screen>
        extends AbstractScreenFacet<S>
        implements EntityAwareScreenFacet<E> {

    protected Class<E> entityClass;

    protected EntityPicker<E> entityPicker;
    protected ListComponent<E> listComponent;
    protected CollectionContainer<E> container;

    @Override
    public void setEntityClass(@Nullable Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Nullable
    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public void setListComponent(@Nullable ListComponent<E> listComponent) {
        this.listComponent = listComponent;
    }

    @Nullable
    @Override
    public ListComponent<E> getListComponent() {
        return listComponent;
    }

    @Override
    public void setEntityPicker(@Nullable EntityPicker<E> entityPicker) {
        this.entityPicker = entityPicker;
    }

    @Nullable
    @Override
    public EntityPicker<E> getEntityPicker() {
        return entityPicker;
    }

    @Override
    public void setContainer(@Nullable CollectionContainer<E> container) {
        this.container = container;
    }

    @Nullable
    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }
}
