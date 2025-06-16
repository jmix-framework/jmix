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
import io.jmix.flowui.UiViewProperties;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;

/**
 * Provides shortcut aliases for view-related actions.
 * <p>
 * Maps common view actions (like save and close) to their corresponding shortcut
 * combinations from {@link UiViewProperties}.
 */
@Component("flowui_ViewsShortcutAliasProvider")
public class ViewsShortcutAliasProvider implements ShortcutAliasProvider<UiViewProperties> {

    protected final Map<String, Function<UiViewProperties, String>> VIEWS_SHORTCUT_ALIASES =
            ImmutableMap.<String, Function<UiViewProperties, String>>builder()
                    .put("SAVE_SHORTCUT", UiViewProperties::getSaveShortcut)
                    .put("CLOSE_SHORTCUT", UiViewProperties::getCloseShortcut)
                    .build();

    protected final UiViewProperties uiViewProperties;

    public ViewsShortcutAliasProvider(UiViewProperties uiViewProperties) {
        this.uiViewProperties = uiViewProperties;
    }

    @Override
    public Map<String, Function<UiViewProperties, String>> getAliases() {
        return VIEWS_SHORTCUT_ALIASES;
    }

    @Override
    public UiViewProperties getPropertyClass() {
        return uiViewProperties;
    }
}
