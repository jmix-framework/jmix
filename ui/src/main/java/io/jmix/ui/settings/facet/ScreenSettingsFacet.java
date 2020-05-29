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

package io.jmix.ui.settings.facet;

import io.jmix.ui.component.Accordion;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Facet;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.Window;
import io.jmix.ui.screen.Screen.AfterDetachEvent;
import io.jmix.ui.screen.Screen.AfterShowEvent;
import io.jmix.ui.screen.Screen.BeforeShowEvent;
import io.jmix.ui.settings.ScreenSettings;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides ability to save and apply component settings.
 *
 * @see ComponentSettings
 * @see ComponentSettingsBinder
 */
public interface ScreenSettingsFacet extends Facet {

    /**
     * @return true if facet should apply and save settings for all supported component in the screen. False by default.
     */
    boolean isAuto();

    /**
     * Set to true if facet should apply and save settings for all supported component in the screen. False by default.
     *
     * @param auto whether facet should include all components for saving settings
     */
    void setAuto(boolean auto);

    /**
     * Adds component ids that should be handled when {@link #isAuto()} returns false.
     * <p>
     * Note, component must be attached to the Window, otherwise it will be ignored.
     *
     * @param ids component ids
     */
    void addComponentIds(String... ids);

    /**
     * @return list of component ids that should be handled when {@link #isAuto()} returns false.
     */
    Set<String> getComponentIds();

    /**
     * Collection depends on {@link #isAuto()} property. If {@link #isAuto()} returns true collection will be
     * filled by {@link Window}'s components, otherwise collection will be filled by components were added by
     * {@link #addComponentIds(String...)}.
     *
     * @return components collection that is used for applying and saving settings.
     */
    Collection<Component> getComponents();

    /**
     * @return screen settings or null if facet is not attached to the screen
     */
    @Nullable
    ScreenSettings getSettings();

    /**
     * Applies screen settings. By default facet applies setting on {@link AfterShowEvent}.
     *
     * @param settings screen settings
     */
    void applySettings(ScreenSettings settings);

    /**
     * Applies screen settings for the components collection. Window must contains these components, otherwise they
     * will be ignored.
     *
     * @param components components to apply
     * @param settings   screen settings
     */
    void applySettings(Collection<Component> components, ScreenSettings settings);

    /**
     * Applies data loading settings. By default facet applies data loading settings on {@link BeforeShowEvent}.
     *
     * @param settings screen settings
     */
    void applyDataLoadingSettings(ScreenSettings settings);

    /**
     * Applies data loading settings for the components collection. Window must contains these components, otherwise
     * they will be ignored.
     *
     * @param components components to apply
     * @param settings   screen settings
     */
    void applyDataLoadingSettings(Collection<Component> components, ScreenSettings settings);

    /**
     * Saves and persist settings. By default facet saves settings on {@link AfterDetachEvent}.
     *
     * @param settings screen settings
     */
    void saveSettings(ScreenSettings settings);

    /**
     * Saves and persist settings for the components collection. Window must contains these components, otherwise
     * they will be ignored.
     *
     * @param components components to save
     * @param settings   screen settings
     */
    void saveSettings(Collection<Component> components, ScreenSettings settings);

    /**
     * @return apply settings delegate or null if not set
     */
    @Nullable
    Consumer<SettingsContext> getApplySettingsDelegate();

    /**
     * Sets apply settings delegate. It will replace default behavior of facet and will be invoked on
     * {@link AfterShowEvent}.
     * <p>
     * Note, it also will be invoked when lazy tab from {@link TabSheet} or {@link Accordion} is opened.
     * <p>
     * For instance:
     * <pre>{@code
     * @Install(to = "settingsFacet", subject = "applySettingsDelegate")
     * private void onApplySetting(SettingsSet settings) {
     *     settingsFacet.applySettings(settings);
     * }
     * }
     * </pre>
     *
     * @param delegate apply settings delegate
     */
    void setApplySettingsDelegate(Consumer<SettingsContext> delegate);

    /**
     * @return apply data loading settings delegate or null if not set
     */
    @Nullable
    Consumer<SettingsContext> getApplyDataLoadingSettingsDelegate();

    /**
     * Sets apply data loading settings delegate. It will replace default behavior of facet and will be invoked on
     * {@link BeforeShowEvent}.
     * <p>
     * For instance:
     * <pre>{@code
     * @Install(to = "settingsFacet", subject = "applyDataLoadingSettingsDelegate")
     * private void onApplyDataLoadingSetting(SettingsSet settings) {
     *     settingsFacet.applyDataLoadingSettings(settings);
     * }
     * }
     * </pre>
     *
     * @param delegate apply settings delegate
     */
    void setApplyDataLoadingSettingsDelegate(Consumer<SettingsContext> delegate);

    /**
     * @return save settings delegate or null if not set
     */
    @Nullable
    Consumer<SettingsContext> getSaveSettingsDelegate();

    /**
     * Set save settings delegate. It will replace default behavior of facet and will be invoked on
     * {@link AfterDetachEvent}.
     * <p>
     * For instance:
     * <pre>{@code
     * @Install(to = "settingsFacet", subject = "saveSettingsDelegate")
     * private void onSaveSetting(SettingsSet settings) {
     *     settingsFacet.saveSettings(settings);
     * }
     * }
     * </pre>
     *
     * @param delegate save settings delegate
     */
    void setSaveSettingsDelegate(Consumer<SettingsContext> delegate);

    /**
     * Provides information about source component and its child components.
     */
    class SettingsContext {

        protected Component source;
        protected Collection<Component> components;
        protected ScreenSettings screenSettings;

        public SettingsContext(Component source, Collection<Component> components, ScreenSettings screenSettings) {
            this.source = source;
            this.components = components;
            this.screenSettings = screenSettings;
        }

        /**
         * @return {@link Window} on opening/closing screen. Return {@link TabSheet} or {@link Accordion}
         * if window has lazy tab and it is opened.
         */
        public Component getSource() {
            return source;
        }

        /**
         * @return child components of source component. For  {@link TabSheet} and {@link Accordion} it will return
         * components from lazy tab.
         */
        public Collection<Component> getComponents() {
            return components;
        }

        /**
         * @return screen settings
         */
        public ScreenSettings getScreenSettings() {
            return screenSettings;
        }
    }
}
