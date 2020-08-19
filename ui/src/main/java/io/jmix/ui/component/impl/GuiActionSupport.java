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

package io.jmix.ui.component.impl;

import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.annotation.Lookup;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.action.entitypicker.OpenAction;
import io.jmix.ui.component.EntityPicker;
import io.jmix.ui.component.data.ValueSource;
import io.jmix.ui.component.data.meta.EntityValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains utility methods used by GUI actions.
 */
@SuppressWarnings("rawtypes")
@Component(GuiActionSupport.NAME)
public class GuiActionSupport {

    public static final String NAME = "ui_GuiActionSupport";

    @Autowired
    protected FetchPlanRepository viewRepository;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Actions actions;

    /**
     * Adds actions specified in {@link Lookup} annotation on entity attribute to the given {@link EntityPicker}.
     *
     * @param entityPicker field
     * @return true if actions have been added
     */
    public boolean createActionsByMetaAnnotations(EntityPicker entityPicker) {
        ValueSource valueSource = entityPicker.getValueSource();
        if (!(valueSource instanceof EntityValueSource)) {
            return false;
        }

        EntityValueSource entityValueSource = (EntityValueSource) entityPicker.getValueSource();
        MetaPropertyPath mpp = entityValueSource.getMetaPropertyPath();
        if (mpp == null) {
            return false;
        }

        String[] actionIds = (String[]) metadataTools
                .getMetaAnnotationAttributes(mpp.getMetaProperty().getAnnotations(), Lookup.class)
                .get("actions");

        if (actionIds != null && actionIds.length > 0) {
            for (String actionId : actionIds) {
                switch (actionId) {
                    case "lookup":
                        entityPicker.addAction(actions.create(LookupAction.ID));
                        break;

                    case "open":
                        entityPicker.addAction(actions.create(OpenAction.ID));
                        break;

                    case "clear":
                        entityPicker.addAction(actions.create(EntityClearAction.ID));
                        break;

                    default:
                        LoggerFactory.getLogger(GuiActionSupport.class)
                                .warn("Unsupported EntityPicker action type " + actionId);
                }
            }

            return true;
        }

        return false;
    }
}
