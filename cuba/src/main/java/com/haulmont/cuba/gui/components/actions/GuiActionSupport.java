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

package com.haulmont.cuba.gui.components.actions;

import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import io.jmix.core.*;
import io.jmix.ui.Actions;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Contains utility methods used by GUI actions.
 */
@Component(GuiActionSupport.NAME)
public class GuiActionSupport {

    public static final String NAME = "cuba_GuiActionSupport";

    @Inject
    protected FetchPlanRepository fetchPlanRepository;
    @Inject
    protected EntityStates entityStates;
    @Inject
    protected DynamicAttributesGuiTools dynamicAttributesGuiTools;
    @Inject
    protected DataManager dataManager;

    @Inject
    protected Actions actions;

    /**
     * Returns an entity reloaded with the view of the target datasource if it is wider than the set of attributes
     * that is loaded in the given entity instance. The entity is also reloaded if the target datasource requires
     * dynamic attributes and the entity instance does not contain them.
     */
    public Entity reloadEntityIfNeeded(Entity entity, Datasource targetDatasource) {
        boolean needDynamicAttributes = targetDatasource.getLoadDynamicAttributes();
        boolean dynamicAttributesAreLoaded = dynamicAttributesGuiTools.hasDynamicAttributes(entity);

        FetchPlan fetchPlan = targetDatasource.getView();
        if (fetchPlan == null) {
            fetchPlan = fetchPlanRepository.getFetchPlan(entity.getClass(), FetchPlan.LOCAL);
        }

        if (!entityStates.isLoadedWithFetchPlan(entity, fetchPlan)) {
            entity = dataManager.load(Id.of(entity))
                    .fetchPlan(fetchPlan)
                    .dynamicAttributes(needDynamicAttributes)
                    .optional().orElse(null);
        } else if (needDynamicAttributes && !dynamicAttributesAreLoaded) {
            dynamicAttributesGuiTools.reloadDynamicAttributes(entity);
        }
        return entity;
    }
}
