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
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.impl.SettingsFacetUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Internal
public final class TabSheetUtils {

    public static void updateTabContent(JmixTabSheet tabSheet, Tab tab, Component content) {
        tabSheet.updateTabContent(tab, content);
    }

    public static void applySettingsToTabContent(JmixTabSheet tabSheet, Tab tab) {
        View<?> view = UiComponentUtils.getView(tabSheet);
        Component tabContent = tabSheet.getContentByTab(tab);
        List<Component> tabComponents = new ArrayList<>();
        if (UiComponentUtils.isContainer(tabContent)) {
            tabComponents.addAll(UiComponentUtils.getComponents(tabContent));
        } else {
            tabComponents.add(tabContent);
        }

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
                            new ArrayList<>(tabComponents),
                            settingsFacet.getSettings()));
                } else {
                    SettingsFacetUtils.applySettings(settingsFacet, tabComponents);
                }
            }
        });
    }
}
