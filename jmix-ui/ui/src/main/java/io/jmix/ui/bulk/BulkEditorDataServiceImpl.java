/*
 * Copyright (c) 2008-2018 Haulmont.
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

package io.jmix.ui.bulk;

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.entity.EntityValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("ui_BulkEditorDataService")
public class BulkEditorDataServiceImpl implements BulkEditorDataService {


    protected DataManager dataManager;

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public <E> List<E> reload(LoadDescriptor<E> loadDescriptor) {
        List<Object> ids = loadDescriptor.getSelectedItems().stream()
                .map(EntityValues::getId)
                .collect(Collectors.toList());

        LoadContext<E> loadContext = new LoadContext<>(loadDescriptor.getMetaClass());
        loadContext.setIds(ids);
        loadContext.setHint("jmix.softDeletion", false);
        loadContext.setFetchPlan(loadDescriptor.getFetchPlan());

        return dataManager.loadList(loadContext);
    }
}