/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader;

import com.google.common.collect.ImmutableMap;
import io.jmix.flowui.UiComponentProperties;
import io.jmix.flowui.UiViewProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

@Component("flowui_PropertyShortcutLoader")
public class PropertyShortcutCombinationLoader {

    protected static final Map<String, Function<UiComponentProperties, String>> COMPONENTS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<UiComponentProperties, String>>builder()
                    .put("GRID_CREATE_SHORTCUT", UiComponentProperties::getGridCreateShortcut)
                    .put("GRID_ADD_SHORTCUT", UiComponentProperties::getGridAddShortcut)
                    .put("GRID_EDIT_SHORTCUT", UiComponentProperties::getGridEditShortcut)
                    .put("GRID_READ_SHORTCUT", UiComponentProperties::getGridReadShortcut)
                    .put("GRID_REMOVE_SHORTCUT", UiComponentProperties::getGridRemoveShortcut)
                    .put("PICKER_LOOKUP_SHORTCUT", UiComponentProperties::getPickerLookupShortcut)
                    .put("PICKER_OPEN_SHORTCUT", UiComponentProperties::getPickerOpenShortcut)
                    .put("PICKER_CLEAR_SHORTCUT", UiComponentProperties::getPickerClearShortcut)
                    .build();

    protected static final Map<String, Function<UiViewProperties, String>> VIEWS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<UiViewProperties, String>>builder()
                    .put("SAVE_SHORTCUT", UiViewProperties::getSaveShortcut)
                    .put("CLOSE_SHORTCUT", UiViewProperties::getCloseShortcut)
                    .build();

    protected UiComponentProperties componentProperties;
    protected UiViewProperties viewProperties;

    public PropertyShortcutCombinationLoader(UiComponentProperties componentProperties, UiViewProperties viewProperties) {
        this.componentProperties = componentProperties;
        this.viewProperties = viewProperties;
    }

    public boolean contains(String alias) {
        return COMPONENTS_SHORTCUT_ALIASES.containsKey(alias)
                || VIEWS_SHORTCUT_ALIASES.containsKey(alias);
    }

    public String getShortcut(String alias) {
        Function<UiComponentProperties, String> componentsShortcut = COMPONENTS_SHORTCUT_ALIASES.get(alias);
        if (componentsShortcut != null) {
            return componentsShortcut.apply(componentProperties);
        }

        Function<UiViewProperties, String> viewsShortcut = VIEWS_SHORTCUT_ALIASES.get(alias);
        if (viewsShortcut != null) {
            return viewsShortcut.apply(viewProperties);
        }

        throw new IllegalStateException(String.format("There is no shortcutCombination for alias '%s'", alias));
    }
}
