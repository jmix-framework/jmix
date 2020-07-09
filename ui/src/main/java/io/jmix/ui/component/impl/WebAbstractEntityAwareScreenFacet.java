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

import io.jmix.core.JmixEntity;
import io.jmix.ui.component.EntityAwareScreenFacet;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.Screen;

public abstract class WebAbstractEntityAwareScreenFacet<E extends JmixEntity, S extends Screen>
        extends WebAbstractScreenFacet<S>
        implements EntityAwareScreenFacet<E> {

    protected Class<E> entityClass;

    protected EntityPicker<E> entityPicker;
    protected ListComponent<E> listComponent;
    protected CollectionContainer<E> container;

    @Override
    public void setEntityClass(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    public void setListComponent(ListComponent<E> listComponent) {
        this.listComponent = listComponent;
    }

    @Override
    public ListComponent<E> getListComponent() {
        return listComponent;
    }

    @Override
    public void setEntityPicker(EntityPicker<E> entityPicker) {
        this.entityPicker = entityPicker;
    }

    @Override
    public EntityPicker<E> getEntityPicker() {
        return entityPicker;
    }

    @Override
    public void setContainer(CollectionContainer<E> container) {
        this.container = container;
    }

    @Override
    public CollectionContainer<E> getContainer() {
        return container;
    }
}
