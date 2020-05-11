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

package com.haulmont.cuba.web.components.ds.api.consistency;

import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsBuilder;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.web.components.AbstractComponentTest;
import io.jmix.core.FetchPlan;
import io.jmix.core.Entity;
import com.haulmont.cuba.core.model.common.Group;

import java.util.UUID;

@SuppressWarnings("rawtypes")
public abstract class AbstractComponentDsTest extends AbstractComponentTest {

    @SuppressWarnings("unchecked")
    protected <E extends Entity> Datasource<E> createTestDatasource(Class<E> entityClass) {
        Datasource<E> datasource = new DsBuilder()
                .setId("testDs")
                .setJavaClass(entityClass)
                .setView(viewRepository.getFetchPlan(entityClass, FetchPlan.LOCAL))
                .buildDatasource();

        E entity = metadata.create(entityClass);
        datasource.setItem(entity);

        ((DatasourceImpl) datasource).valid();

        return datasource;
    }

    protected CollectionDatasource<Group, UUID> getTestCollectionDatasource() {
        // noinspection unchecked
        CollectionDatasource<Group, UUID> collectionDatasource = new DsBuilder()
                .setId("testDs")
                .setJavaClass(Group.class)
                .setView(viewRepository.getFetchPlan(Group.class, FetchPlan.LOCAL))
                .setRefreshMode(CollectionDatasource.RefreshMode.NEVER)
                .setAllowCommit(false)
                .buildCollectionDatasource();

        for (int i = 0; i < 3; i++) {
            Group group = metadata.create(Group.class);
            group.setName("Group #" + (i + 1));

            Group parentGroup = metadata.create(Group.class);
            parentGroup.setName("Parent group #" + (i + 1));
            group.setParent(parentGroup);

            collectionDatasource.addItem(group);
        }
        ((CollectionDatasourceImpl) collectionDatasource).valid();

        return collectionDatasource;
    }
}
