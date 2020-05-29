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

package com.haulmont.cuba.gui.xml.data;

import com.haulmont.cuba.gui.components.PickerField;
import io.jmix.ui.GuiDevelopmentException;
import io.jmix.ui.action.Action;
import io.jmix.ui.gui.OpenType;
import io.jmix.ui.screen.compatibility.CubaLegacyFrame;
import io.jmix.ui.xml.layout.ComponentLoader;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import java.util.Optional;

public final class ComponentLoaderHelper {

    public static Optional<Action> loadLegacyPickerAction(PickerField actionsHolder,
                                                          Element element, ComponentLoader.Context context,
                                                          String actionId) {
        if (StringUtils.isBlank(element.attributeValue("invoke")) && isLegacyFrame(context)) {
            // Try to create a standard picker action
            for (PickerField.ActionType type : PickerField.ActionType.values()) {
                if (type.getId().equals(actionId)) {
                    Action action = type.createAction(actionsHolder);
                    if (type != PickerField.ActionType.LOOKUP && type != PickerField.ActionType.OPEN) {
                        return Optional.of(action);
                    }

                    String openTypeString = element.attributeValue("openType");
                    if (openTypeString == null) {
                        return Optional.of(action);
                    }

                    OpenType openType;
                    try {
                        openType = OpenType.valueOf(openTypeString);
                    } catch (IllegalArgumentException e) {
                        throw new GuiDevelopmentException(
                                String.format("Unknown open type: '%s' for action: '%s'", openTypeString, actionId),
                                context);
                    }

                    if (action instanceof PickerField.LookupAction) {
                        ((PickerField.LookupAction) action).setLookupScreenOpenType(openType);
                    } else if (action instanceof PickerField.OpenAction) {
                        ((PickerField.OpenAction) action).setEditScreenOpenType(openType);
                    }
                    return Optional.of(action);
                }
            }
        }

        return Optional.empty();
    }

    public static boolean isLegacyFrame(ComponentLoader.Context context) {
        return context instanceof ComponentLoader.ComponentContext
                && ((ComponentLoader.ComponentContext) context).getFrame().getFrameOwner() instanceof CubaLegacyFrame;
    }
}
