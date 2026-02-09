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
import io.jmix.flowui.facet.FragmentSettingsFacet;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.ViewSettingsFacet;
import io.jmix.flowui.facet.impl.SettingsFacetUtils;
import io.jmix.flowui.facet.settings.UiComponentSettings;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Internal
public final class TabSheetUtils {

    public static void updateTabContent(JmixTabSheet tabSheet, Tab tab, Component content) {
        tabSheet.updateTabContent(tab, content);
    }

    public static void applySettingsToTabContent(JmixTabSheet tabSheet, Tab tab) {
        Optional<? extends SettingsFacet<?>> settingsFacet;
        Component owner;

        Fragment<?> fragment = UiComponentUtils.findFragment(tabSheet);
        if (fragment != null) {
            settingsFacet = getFragmentFacet(fragment);
            owner = fragment;
        } else {
            View<?> view = UiComponentUtils.getView(tabSheet);
            settingsFacet = getViewFacet(view);
            owner = view;
        }

        Component tabContent = tabSheet.getContentByTab(tab);

        List<Component> tabComponents = new ArrayList<>();
        if (UiComponentUtils.isContainer(tabContent)) {
            tabComponents.addAll(UiComponentUtils.getComponents(tabContent));
        } else {
            tabComponents.add(tabContent);
        }

        settingsFacet.ifPresent(facet -> processSettingsFacet(facet, owner, tabComponents));
    }

    private static Optional<? extends SettingsFacet<?>> getFragmentFacet(Fragment<?> fragment) {
        return FragmentUtils.getFragmentFacets(fragment).getFacets()
                .filter(facet -> facet instanceof FragmentSettingsFacet)
                .map(FragmentSettingsFacet.class::cast)
                .findAny();
    }

    private static Optional<? extends SettingsFacet<?>> getViewFacet(View<?> view) {
        return ViewControllerUtils.getViewFacets(view).getFacets()
                .filter(facet -> facet instanceof ViewSettingsFacet)
                .map(ViewSettingsFacet.class::cast)
                .findAny();
    }

    private static <S extends UiComponentSettings<S>> void processSettingsFacet(SettingsFacet<S> settingsFacet, Component owner,
                                                                                List<Component> tabComponents) {
        if (settingsFacet.getSettings() == null) {
            throw new IllegalStateException("SettingsFacet is not attached to the view");
        }

        Consumer<? super SettingsFacet.SettingsContext<S>> applySettingsDelegate =
                settingsFacet.getApplySettingsDelegate();

        if (applySettingsDelegate != null) {
            SettingsFacet.SettingsContext<S> context = new SettingsFacet.SettingsContext<>(
                    owner,
                    new ArrayList<>(tabComponents),
                    settingsFacet.getSettings());

            applySettingsDelegate.accept(context);
        } else {
            SettingsFacetUtils.applySettings(settingsFacet, tabComponents);
        }
    }
}
