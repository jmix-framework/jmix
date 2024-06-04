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

package io.jmix.dynattrflowui.impl;

import com.vaadin.flow.component.HasValue;
import io.jmix.core.*;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.dynattr.DynAttrManager;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.dynattr.DynamicAttributesState;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.HasLoader;
import io.jmix.flowui.view.builder.EditedEntityTransformer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@Component("dynat_DynAttrAwareEntityTransformer")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 10)
public class DynAttrAwareEntityTransformer implements EditedEntityTransformer {

    private final EntityStates entityStates;
    private final DataManager dataManager;
    private final DynAttrManager dynAttrManager;
    private final AccessConstraintsRegistry accessConstraintsRegistry;

    public DynAttrAwareEntityTransformer(EntityStates entityStates,
                                         DataManager dataManager,
                                         DynAttrManager dynAttrManager,
                                         AccessConstraintsRegistry accessConstraintsRegistry) {
        this.entityStates = entityStates;
        this.dataManager = dataManager;
        this.dynAttrManager = dynAttrManager;
        this.accessConstraintsRegistry = accessConstraintsRegistry;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public <E> E transformForCollectionContainer(E editedEntity, CollectionContainer<E> container) {
        boolean needDynamicAttributes = false;
        boolean dynamicAttributesAreLoaded = true;

        DynamicAttributesState state = EntitySystemAccess.getExtraState(editedEntity, DynamicAttributesState.class);
        if (state != null) {
            dynamicAttributesAreLoaded = state.getDynamicAttributes() != null;
        }

        if (container instanceof HasLoader) {
            DataLoader loader = ((HasLoader) container).getLoader();
            if (loader instanceof CollectionLoader) {
                Map<String, Serializable> hints = loader.getHints();

                // todo hints is always true
                //noinspection ConstantValue
                needDynamicAttributes = hints != null
                        && Boolean.TRUE.equals(hints.get(DynAttrQueryHints.LOAD_DYN_ATTR));
            }
        }

        FetchPlan fetchPlan = container.getFetchPlan();
        if (fetchPlan != null && !entityStates.isLoadedWithFetchPlan(editedEntity, fetchPlan)) {
            return dataManager.load(Id.of(editedEntity))
                    .fetchPlan(fetchPlan)
                    .hint(DynAttrQueryHints.LOAD_DYN_ATTR, needDynamicAttributes)
                    .one();
        } else if (needDynamicAttributes && !dynamicAttributesAreLoaded) {
            dynAttrManager.loadValues(Collections.singletonList(editedEntity), fetchPlan, accessConstraintsRegistry.getConstraints());
            return editedEntity;
        } else {
            return editedEntity;
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public <E> E transformForField(E editedEntity, HasValue<?, E> field) {
        return editedEntity;
    }


}
