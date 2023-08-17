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

package io.jmix.flowui.facet;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import io.jmix.flowui.component.details.JmixDetails;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.component.binder.ComponentSettingsBinder;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.View.BeforeShowEvent;
import io.jmix.flowui.view.View.ReadyEvent;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Provides ability to save component states when {@link View} is closed and restore them
 * when it's opened again.
 * <p>
 * For instance, it can be 'opened' state from {@link JmixDetails} or columns order in {@link DataGrid}.
 * <p>
 * Note, facet works with components that contain an id and have {@link ComponentSettingsBinder}.
 * Otherwise, it cannot match saved settings with component.
 */
public interface SettingsFacet extends Facet {

    /**
     * @return the mode in which facet is worked. {@code true} if facet includes all components and {@code false}
     * if only specified components by {@link #addComponentIds(String...)} will be managed.
     */
    boolean isAuto();

    /**
     * Defines in which mode facet will work. If 'auto' is set to {@code true} it means that all components
     * that contain an id and have {@link ComponentSettingsBinder} will be managed by facet.
     * <p>
     * If 'auto' mode is disabled (set to {@code false}), only specified components by
     * {@link #addComponentIds(String...)} will be managed.
     * <p>
     * The default value is {@code false}.
     *
     * @param auto whether facet should include all components
     */
    void setAuto(boolean auto);

    /**
     * @return {@link View} settings or {@code null} if facet is not attached to the {@link View}
     */
    @Nullable
    ViewSettings getSettings();

    /**
     * Restores settings for components. Facet applies settings on {@link View}'s {@link ReadyEvent}.
     */
    void applySettings();

    /**
     * Restores settings that related with data loading. For instance, it applies sort columns and its
     * direction in {@link DataGrid} before data loading.
     * <p>
     * Facet applies data loading settings on {@link View}'s {@link BeforeShowEvent}.
     */
    void applyDataLoadingSettings();

    /**
     * Persists settings to store. Facet saves settings on {@link View}'s {@link DetachEvent}.
     */
    void saveSettings();

    /**
     * Adds component ids that should be managed when {@link #isAuto()} returns {@code false}.
     * <p>
     * Note, component must be attached to the {@link View}, otherwise it will be ignored.
     *
     * @param ids component ids
     */
    void addComponentIds(String... ids);

    /**
     * @return set of component ids that should be handled when {@link #isAuto()} returns {@code false}
     */
    Set<String> getComponentIds();

    /**
     * Adds component ids that should be excluded from applying and saving settings. Excluding is
     * applied despite {@link #isAuto()} mode and has priority over explicitly added component ids
     * {@link #addComponentIds(String...)}.
     *
     * @param ids component ids to exclude
     */
    void addExcludedComponentIds(String... ids);

    /**
     * @return set of component ids that should be excluded from applying and saving settings
     */
    Set<String> getExcludedComponentIds();

    /**
     * Collection depends on {@link #isAuto()} property. If {@link #isAuto()} returns {@code true}, collection will be
     * filled by {@link View}'s components, otherwise collection will be filled by components that explicitly added by
     * {@link #addComponentIds(String...)}.
     *
     * @return components collection that is used for applying and saving settings
     */
    Collection<Component> getManagedComponents();

    /**
     * @return apply settings delegate or {@code null} if not set
     */
    @Nullable
    Consumer<SettingsContext> getApplySettingsDelegate();

    /**
     * Sets handler that should be invoked instead of default facet's logic for applying settings.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "settingsFacet", subject = "applySettingsDelegate")
     * private void onApplySettings(SettingsFacet.SettingsContext settingsContext) {
     *     settingsFacet.applySettings();
     * }
     * </pre>
     *
     * @param delegate handler to set
     */
    void setApplySettingsDelegate(@Nullable Consumer<SettingsContext> delegate);

    /**
     * @return apply data loading settings delegate or {@code null} if not set
     */
    @Nullable
    Consumer<SettingsContext> getApplyDataLoadingSettingsDelegate();

    /**
     * Sets handler that should be invoked instead of default facet's logic for applying data loading settings.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "settingsFacet", subject = "applyDataLoadingSettingsDelegate")
     * private void onApplyDataLoadingSettings(SettingsFacet.SettingsContext settingsContext) {
     *     settingsFacet.applyDataLoadingSettings();
     * }
     * </pre>
     *
     * @param delegate handler to set
     */
    void setApplyDataLoadingSettingsDelegate(@Nullable Consumer<SettingsContext> delegate);

    /**
     * @return save settings delegate or {@code null} if not set
     */
    @Nullable
    Consumer<SettingsContext> getSaveSettingsDelegate();

    /**
     * Sets handler that should be invoked instead of default facet's logic for saving settings.
     * <p>
     * For instance:
     * <pre>
     * &#64;Install(to = "settingsFacet", subject = "saveSettingsDelegate")
     * private void onSaveSettings(SettingsFacet.SettingsContext settingsContext) {
     *     settingsFacet.saveSettings();
     * }
     * </pre>
     *
     * @param delegate handler to set
     */
    void setSaveSettingsDelegate(@Nullable Consumer<SettingsContext> delegate);

    /**
     * Provides information about source component and components that should be managed by facet.
     */
    class SettingsContext {

        protected Component source;
        protected Collection<Component> components;
        protected ViewSettings viewSettings;

        public SettingsContext(Component source, Collection<Component> components, ViewSettings viewSettings) {
            this.source = source;
            this.components = components;
            this.viewSettings = viewSettings;
        }

        /**
         * @return the source component, e.g. {@link View}
         */
        public Component getSource() {
            return source;
        }

        /**
         * @return components that should be managed by facet
         */
        public Collection<Component> getComponents() {
            return components;
        }

        /**
         * @return {@link View} settings
         */
        public ViewSettings getViewSettings() {
            return viewSettings;
        }
    }
}
