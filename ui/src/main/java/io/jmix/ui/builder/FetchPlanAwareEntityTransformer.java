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

package io.jmix.ui.builder;

import io.jmix.core.*;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("ui_FetchPlanAwareEntityTransformer")
@Order(100)
public class FetchPlanAwareEntityTransformer implements EditedEntityTransformer {

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DataManager dataManager;

    @Override
    public <E extends Entity> E transformForCollectionContainer(E editedEntity, CollectionContainer<E> container) {
        FetchPlan fetchPlan = container.getFetchPlan();
        if (fetchPlan != null && !entityStates.isLoadedWithFetchPlan(editedEntity, fetchPlan)) {
            return dataManager.load(Id.of(editedEntity)).fetchPlan(fetchPlan).one();
        } else {
            return editedEntity;
        }
    }

    @Override
    public <E extends Entity> E transformForField(E editedEntity, HasValue<E> field) {
        return editedEntity;
    }
}
