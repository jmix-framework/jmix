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

package io.jmix.dynattrui.impl;

import io.jmix.core.*;
import io.jmix.dynattr.DynAttrManager;
import io.jmix.dynattr.DynamicAttributesState;
import io.jmix.ui.builder.EditedEntityTransformer;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataLoader;
import io.jmix.ui.model.HasLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component("dynattrui_DynAttrAwareEntityTransformer")
@Order(110)
public class DynAttrAwareEntityTransformer implements EditedEntityTransformer {

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private DynAttrManager dynAttrManager;

    @Override
    public <E extends JmixEntity> E transformForCollectionContainer(E editedEntity, CollectionContainer<E> container) {
        boolean needDynamicAttributes = false;
        boolean dynamicAttributesAreLoaded = true;

        DynamicAttributesState state = (DynamicAttributesState) editedEntity.__getEntityEntry().getExtraState(DynamicAttributesState.class);
        if (state != null) {
            dynamicAttributesAreLoaded = state.getDynamicAttributes() != null;
        }

        if (container instanceof HasLoader) {
            DataLoader loader = ((HasLoader) container).getLoader();
            if (loader instanceof CollectionLoader) {
                needDynamicAttributes = ((CollectionLoader) loader).isLoadDynamicAttributes();
            }
        }

        FetchPlan fetchPlan = container.getFetchPlan();
        if (fetchPlan != null && !entityStates.isLoadedWithFetchPlan(editedEntity, fetchPlan)) {
            return dataManager.load(Id.of(editedEntity)).fetchPlan(fetchPlan).dynamicAttributes(needDynamicAttributes).one();
        } else if (needDynamicAttributes && !dynamicAttributesAreLoaded) {
            dynAttrManager.loadValues(Collections.singletonList(editedEntity), fetchPlan);
            return editedEntity;
        } else {
            return editedEntity;
        }
    }

    @Override
    public <E extends JmixEntity> E transformForField(E editedEntity, HasValue<E> field) {
        return editedEntity;
    }
}
