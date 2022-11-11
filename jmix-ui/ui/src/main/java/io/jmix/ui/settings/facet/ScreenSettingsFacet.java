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
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioCollection;
import io.jmix.ui.meta.StudioFacet;
import io.jmix.ui.meta.StudioProperty;
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
@StudioFacet(
        xmlElement = "screenSettings",
        caption = "ScreenSettings",
        category = "Facets",
        description = "Provides ability to save and apply component settings",
        defaultProperty = "auto",
        icon = "io/jmix/ui/icon/facet/screenSettings.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/facets/screen-settings-facet.html"
)
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
    @StudioProperty(defaultValue = "false", initialValue = "true")
    void setAuto(boolean auto);

    /**
     * Adds component ids that should be handled when {@link #isAuto()} returns false.
     * <p>
     * Note, component must be attached to the Window, otherwise it will be ignored.
     *
     * @param ids component ids
     */
    @StudioCollection(xmlElement = "components",
            icon = "io/jmix/ui/icon/element/components.svg",
            itemXmlElement = "component",
            itemCaption = "Component Id",
            itemProperties = {
                    @StudioProperty(name = "id", type = PropertyType.COMPONENT_REF, required = true,
                            options = {"io.jmix.ui.component.Component"}),
                    @StudioProperty(name = "exclude", type = PropertyType.BOOLEAN, defaultValue = "false")
            },
            itemIcon = "io/jmix/ui/icon/element/component.svg"
    )
    void addComponentIds(String... ids);

    /**
     * @return set of component ids that should be handled when {@link #isAuto()} returns false.
     */
    Set<String> getComponentIds();

    /**
     * Adds component ids that should be excluded from applying and saving settings. Excluding is applied despite
     * {@link #isAuto()} mode and has priority over explicitly added component ids {@link #addComponentIds(String...)}.
     *
     * @param ids component ids to exclude
     */
    void excludeComponentIds(String... ids);

    /**
     * @return set of component ids that should be excluded from applying and saving settings.
     */
    Set<String> getExcludedComponentIds();

    /**
     * Collection depends on {@link #isAuto()} property. If {@link #isAuto()} returns true collection will be
     * filled by {@link Window}'s components, otherwise collection will be filled by components were added by
     * {@link #addComponentIds(String...)}.
     *
     * @return components collection that is used for applying and saving settings.
     */
    Collection<Component> getComponents();

    /**
     * @return screen settings or {@code null} if facet is not attached to the screen
     */
    @Nullable
    ScreenSettings getSettings();

    /**
     * Applies screen settings. By default facet applies setting on {@link AfterShowEvent}.
     */
    void applySettings();

    /**
     * Applies screen settings for the components collection. Window must contains these components, otherwise they
     * will be ignored.
     *
     * @param components components to apply
     */
    void applySettings(Collection<Component> components);

    /**
     * Applies data loading settings. By default facet applies data loading settings on {@link BeforeShowEvent}.
     */
    void applyDataLoadingSettings();

    /**
     * Applies data loading settings for the components collection. Window must contains these components, otherwise
     * they will be ignored.
     *
     * @param components components to apply
     */
    void applyDataLoadingSettings(Collection<Component> components);

    /**
     * Saves and persist settings. By default facet saves settings on {@link AfterDetachEvent}.
     */
    void saveSettings();

    /**
     * Saves and persist settings for the components collection. Window must contains these components, otherwise
     * they will be ignored.
     *
     * @param components components to save
     */
    void saveSettings(Collection<Component> components);

    /**
     * @return apply settings delegate or {@code null} if not set
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
     * <pre>
     * &#64;Install(to = "settingsFacet", subject = "applySettingsDelegate")
     * private void onApplySettings(ScreenSettingsFacet.SettingsContext settingsContext) {
     *     settingsFacet.applySettings();
     * }
     * </pre>
     *
     * @param delegate apply settings delegate
     */
    void setApplySettingsDelegate(Consumer<SettingsContext> delegate);

    /**
     * @return apply data loading settings delegate or {@code null} if not set
     */
    @Nullable
    Consumer<SettingsContext> getApplyDataLoadingSettingsDelegate();

    /**
     * Sets apply data loading settings delegate. It will replace default behavior of facet and will be invoked on
     * {@link BeforeShowEvent}.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "settingsFacet", subject = "applyDataLoadingSettingsDelegate")
     * private void onApplyDataLoadingSettings(ScreenSettingsFacet.SettingsContext settingsContext) {
     *     settingsFacet.applyDataLoadingSettings();
     * }
     * </pre>
     *
     * @param delegate apply settings delegate
     */
    void setApplyDataLoadingSettingsDelegate(Consumer<SettingsContext> delegate);

    /**
     * @return save settings delegate or {@code null} if not set
     */
    @Nullable
    Consumer<SettingsContext> getSaveSettingsDelegate();

    /**
     * Set save settings delegate. It will replace default behavior of facet and will be invoked on
     * {@link AfterDetachEvent}.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "settingsFacet", subject = "saveSettingsDelegate")
     * private void onSaveSettings(ScreenSettingsFacet.SettingsContext settingsContext) {
     *     settingsFacet.saveSettings();
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
