/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.xml;

import com.google.common.collect.ImmutableMap;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiScreenProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Class provides predefined shortcuts that can be retrieved by alias.
 */
@Component("ui_PropertyShortcutLoader")
public class PropertyShortcutLoader {

    protected static final Map<String, Function<UiComponentProperties, String>> COMPONENTS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<UiComponentProperties, String>>builder()
                    .put("TABLE_EDIT_SHORTCUT", UiComponentProperties::getTableEditShortcut)
                    .put("TABLE_INSERT_SHORTCUT", UiComponentProperties::getTableInsertShortcut)
                    .put("TABLE_ADD_SHORTCUT", UiComponentProperties::getTableAddShortcut)
                    .put("TABLE_REMOVE_SHORTCUT", UiComponentProperties::getTableRemoveShortcut)
                    .put("NEXT_TAB_SHORTCUT", UiComponentProperties::getMainTabSheetNextTabShortcut)
                    .put("PREVIOUS_TAB_SHORTCUT", UiComponentProperties::getMainTabSheetPreviousTabShortcut)
                    .put("PICKER_LOOKUP_SHORTCUT", UiComponentProperties::getPickerLookupShortcut)
                    .put("PICKER_OPEN_SHORTCUT", UiComponentProperties::getPickerOpenShortcut)
                    .put("PICKER_CLEAR_SHORTCUT", UiComponentProperties::getPickerClearShortcut)
                    .build();

    protected static final Map<String, Function<UiScreenProperties, String>> SCREENS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<UiScreenProperties, String>>builder()
                    .put("COMMIT_SHORTCUT", UiScreenProperties::getCommitShortcut)
                    .put("CLOSE_SHORTCUT", UiScreenProperties::getCloseShortcut)
                    .build();

    protected UiComponentProperties componentProperties;
    protected UiScreenProperties screenProperties;

    @Autowired
    public PropertyShortcutLoader(UiComponentProperties componentProperties,
                                  UiScreenProperties screenProperties) {
        this.componentProperties = componentProperties;
        this.screenProperties = screenProperties;
    }

    /**
     * @param alias shortcut alias
     * @return {@code true} if alias is defined
     */
    public boolean contains(String alias) {
        return COMPONENTS_SHORTCUT_ALIASES.containsKey(alias)
                || SCREENS_SHORTCUT_ALIASES.containsKey(alias);
    }

    /**
     * @param alias shortcut alias
     * @return shortcut combination
     * @throws IllegalStateException if provided alias is not defined
     */
    public String getShortcut(String alias) {
        Function<UiComponentProperties, String> compShortcut = COMPONENTS_SHORTCUT_ALIASES.get(alias);
        if (compShortcut != null) {
            return compShortcut.apply(componentProperties);
        }

        Function<UiScreenProperties, String> screensShortcut = SCREENS_SHORTCUT_ALIASES.get(alias);
        if (screensShortcut != null) {
            return screensShortcut.apply(screenProperties);
        }

        throw new IllegalStateException(String.format("There is no shortcut for alias '%s'", alias));
    }
}
