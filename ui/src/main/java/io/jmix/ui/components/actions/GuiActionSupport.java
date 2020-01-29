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

package io.jmix.ui.components.actions;

import io.jmix.core.EntityStates;
import io.jmix.core.MetadataTools;
import io.jmix.core.ViewRepository;
import io.jmix.core.entity.annotation.Lookup;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.Actions;
import io.jmix.ui.actions.picker.ClearAction;
import io.jmix.ui.actions.picker.LookupAction;
import io.jmix.ui.actions.picker.OpenAction;
import io.jmix.ui.components.PickerField;
import io.jmix.ui.components.data.ValueSource;
import io.jmix.ui.components.data.meta.EntityValueSource;
import io.jmix.ui.screen.compatibility.CubaLegacyFrame;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Contains utility methods used by GUI actions.
 */
@SuppressWarnings("rawtypes")
@Component(GuiActionSupport.NAME)
public class GuiActionSupport {

    public static final String NAME = "jmix_GuiActionSupport";

    @Inject
    protected ViewRepository viewRepository;
    @Inject
    protected EntityStates entityStates;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected Actions actions;

    /**
     * Adds actions specified in {@link Lookup} annotation on entity attribute to the given PickerField.
     *
     * @param pickerField field
     * @return true if actions have been added
     */
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
                if (pickerField.getFrame() != null
                        && pickerField.getFrame().getFrameOwner() instanceof CubaLegacyFrame) {

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
                            pickerField.addAction(actions.create(ClearAction.ID));
                            break;

                        default:
                            LoggerFactory.getLogger(GuiActionSupport.class)
                                    .warn("Unsupported PickerField action type " + actionId);
                            break;
                    }
                }
            }
            return true;
        }
        return false;
    }
}
