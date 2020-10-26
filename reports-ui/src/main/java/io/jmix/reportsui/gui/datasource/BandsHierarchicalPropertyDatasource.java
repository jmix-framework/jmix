/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.reportsui.gui.datasource;

import io.jmix.core.JmixEntity;
import com.haulmont.cuba.gui.data.impl.HierarchicalPropertyDatasourceImpl;

import java.util.Collection;
import java.util.Set;

public class BandsHierarchicalPropertyDatasource<T extends JmixEntity, K> extends HierarchicalPropertyDatasourceImpl<T, K> {
    @Override
    public void committed(Set<JmixEntity> entities) {
        super.committed(entities);
        if (State.VALID.equals(masterDs.getState())) {
            Collection<T> collection = getCollection();
            if (item != null && collection != null) {
                for (T entity : collection) {
                    if (entity.equals(item)) {
                        item = entity;
                    }
                }
            }
        }
    }
}
