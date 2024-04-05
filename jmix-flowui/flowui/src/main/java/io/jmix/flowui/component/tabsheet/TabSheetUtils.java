/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component.tabsheet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.Facet;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Internal
public final class TabSheetUtils {
    public static void updateTabContent(JmixTabSheet tabSheet, Tab tab, Component content) {
        tabSheet.updateTabContent(tab, content);
    }

    public static void applySettingsToTabContent(Tab tab) {
        View<?> view = UiComponentUtils.getView(tab);

        ViewControllerUtils.getViewFacets(view).getFacets().forEach(facet -> {
            if (facet instanceof SettingsFacet settingsFacet) {

                if (settingsFacet.getSettings() == null) {
                    throw new IllegalStateException("SettingsFacet is not attached to the view");
                }

                Consumer<SettingsFacet.SettingsContext> applySettingsDelegate =
                        settingsFacet.getApplySettingsDelegate();

                if (applySettingsDelegate != null) {
                    applySettingsDelegate.accept(new SettingsFacet.SettingsContext(
                            view,
                            tab.getChildren().collect(Collectors.toList()),
                            settingsFacet.getSettings()));
                } else {
                    settingsFacet.saveSettings();
                    settingsFacet.applySettings();
                }
            }
        });
    }
}
