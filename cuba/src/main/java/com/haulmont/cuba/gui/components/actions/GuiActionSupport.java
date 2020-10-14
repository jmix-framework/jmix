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

import com.haulmont.cuba.gui.actions.picker.LookupAction;
import com.haulmont.cuba.gui.actions.picker.OpenAction;
import com.haulmont.cuba.gui.components.PickerField;
import com.haulmont.cuba.gui.components.data.value.DatasourceValueSource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.dynamicattributes.DynamicAttributesGuiTools;
import com.haulmont.cuba.gui.screen.compatibility.LegacyFrame;
import io.jmix.core.*;
import com.haulmont.cuba.core.entity.annotation.Lookup;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.dynattr.DynAttrQueryHints;
import io.jmix.ui.Actions;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains utility methods used by GUI actions.
 */
@Component(GuiActionSupport.NAME)
public class GuiActionSupport {

    public static final String NAME = "cuba_GuiActionSupport";

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected DynamicAttributesGuiTools dynamicAttributesGuiTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
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
                    .hint(DynAttrQueryHints.LOAD_DYN_ATTR, true)
                    .optional().orElse(null);
        } else if (needDynamicAttributes && !dynamicAttributesAreLoaded) {
            dynamicAttributesGuiTools.reloadDynamicAttributes(entity);
        }
        return entity;
    }

    public boolean createActionsByMetaAnnotations(PickerField pickerField) {
        ValueSource valueSource = pickerField.getValueSource();
        if (!(valueSource instanceof EntityValueSource)) {
            return false;
        }

        EntityValueSource entityValueSource = (EntityValueSource) pickerField.getValueSource();
        MetaPropertyPath mpp = entityValueSource.getMetaPropertyPath();
        if (mpp == null) {
            return false;
        }

        String[] actionIds = (String[]) metadataTools
                .getMetaAnnotationAttributes(mpp.getMetaProperty().getAnnotations(), Lookup.class)
                .get("actions");
        if (actionIds != null && actionIds.length > 0) {
            for (String actionId : actionIds) {
                createActionById(pickerField, actionId);
            }
            return true;
        }
        return false;
    }

    public void createActionById(PickerField<?> pickerField, String actionId) {
        if (isInLegacyScreen(pickerField)) {
            // in legacy screens
            for (PickerField.ActionType actionType : PickerField.ActionType.values()) {
                if (actionType.getId().equals(actionId.trim())) {
                    pickerField.addAction(actionType.createAction(pickerField));
                    break;
                }
            }
        } else {
            switch (actionId) {
                case "lookup":
                    pickerField.addAction(actions.create(LookupAction.ID));
                    break;

                case "open":
                    pickerField.addAction(actions.create(OpenAction.ID));
                    break;

                case "clear":
                    pickerField.addAction(actions.create(EntityClearAction.ID));
                    break;

                default:
                    LoggerFactory.getLogger(GuiActionSupport.class)
                            .warn("Unsupported PickerField action type " + actionId);
                    break;
            }
        }
    }

    protected boolean isInLegacyScreen(PickerField<?> pickerField) {
        return pickerField.getFrame() != null
                && pickerField.getFrame().getFrameOwner() instanceof LegacyFrame
                || pickerField.getValueSource() instanceof DatasourceValueSource;
    }
}
