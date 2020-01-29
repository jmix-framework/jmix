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
import io.jmix.core.EntityStates;
import io.jmix.core.MetadataTools;
import io.jmix.core.View;
import io.jmix.core.ViewRepository;
import io.jmix.core.entity.BaseGenericIdEntity;
import io.jmix.core.entity.Entity;
import io.jmix.ui.Actions;
import io.jmix.ui.dynamicattributes.DynamicAttributesGuiTools;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Contains utility methods used by GUI actions.
 */
@Component(GuiActionSupport.NAME)
public class GuiActionSupport {

    public static final String NAME = "cuba_GuiActionSupport";

    @Inject
    protected ViewRepository viewRepository;
    @Inject
    protected EntityStates entityStates;
    @Inject
    protected DynamicAttributesGuiTools dynamicAttributesGuiTools;

    @Inject
    protected Actions actions;

    /**
     * Returns an entity reloaded with the view of the target datasource if it is wider than the set of attributes
     * that is loaded in the given entity instance. The entity is also reloaded if the target datasource requires
     * dynamic attributes and the entity instance does not contain them.
     */
    public Entity reloadEntityIfNeeded(Entity entity, Datasource targetDatasource) {
        boolean needDynamicAttributes = false;
        boolean dynamicAttributesAreLoaded = true;
        if (entity instanceof BaseGenericIdEntity) {
            BaseGenericIdEntity e = (BaseGenericIdEntity) entity;
            // todo dynamic attributes
//            dynamicAttributesAreLoaded = e.getDynamicAttributes() != null;
            needDynamicAttributes = targetDatasource.getLoadDynamicAttributes();
        }

        View view = targetDatasource.getView();
        if (view == null) {
            view = viewRepository.getView(entity.getClass(), View.LOCAL);
        }

        if (!entityStates.isLoadedWithView(entity, view)) {
            entity = targetDatasource.getDsContext().getDataSupplier().reload(entity, view, null, needDynamicAttributes);
        } else if (needDynamicAttributes && !dynamicAttributesAreLoaded) {
            dynamicAttributesGuiTools.reloadDynamicAttributes((BaseGenericIdEntity) entity);
        }
        return entity;
    }
}
