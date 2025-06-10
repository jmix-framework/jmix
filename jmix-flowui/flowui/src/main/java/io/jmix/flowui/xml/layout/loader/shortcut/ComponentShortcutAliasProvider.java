/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.shortcut;

import com.google.common.collect.ImmutableMap;
import io.jmix.flowui.UiComponentProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Provides shortcut aliases for UI component-related actions.
 * <p>
 * Maps common component actions (like grid operations and picker actions) to their
 * corresponding shortcut combinations from {@link UiComponentProperties}.
 */
@Component("flowui_ComponentShortcutAliasesProvider")
public class ComponentShortcutAliasProvider implements ShortcutAliasProvider<UiComponentProperties> {

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

    protected final UiComponentProperties uiComponentProperties;

    public ComponentShortcutAliasProvider(UiComponentProperties uiComponentProperties) {
        this.uiComponentProperties = uiComponentProperties;
    }

    @Override
    public Map<String, Function<UiComponentProperties, String>> getAliases() {
        return COMPONENTS_SHORTCUT_ALIASES;
    }

    @Override
    public UiComponentProperties getPropertyClass() {
        return uiComponentProperties;
    }
}
