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

package com.haulmont.cuba;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.List;

@ConfigurationProperties(prefix = "jmix.cuba")
@ConstructorBinding
public class CubaProperties {

    boolean dataManagerBeanValidation;
    List<String> disableEscapingLikeForDataStores;
    boolean allowQueryFromSelected;
    boolean genericFilterManualApplyRequired;
    int genericFilterColumnsCount;
    String filterApplyShortcut;
    String filterSelectShortcut;
    String genericFilterConditionsLocation;
    String genericFilterControlsLayout;
    int genericFilterPopupListSize;
    boolean genericFilterChecking;
    int genericFilterPropertiesHierarchyDepth;
    boolean genericFilterTrimParamValues;
    String genericFilterMaxResultsOptions;
    boolean genericFilterApplyImmediately;
    boolean localeSelectVisible;
    boolean showFolderIcons;
    boolean foldersPaneVisibleByDefault;
    boolean foldersPaneEnabled;
    int appFoldersRefreshPeriodSec;
    String appFolderEditWindow;
    String folderEditWindow;
    int foldersPaneDefaultWidth;
    boolean rememberMeEnabled;
    boolean poweredByLinkVisible;
    String embeddedResourcesRoot;

    public CubaProperties(
            boolean dataManagerBeanValidation,
            List<String> disableEscapingLikeForDataStores,
            @DefaultValue("true") boolean allowQueryFromSelected,
            boolean genericFilterManualApplyRequired,
            @DefaultValue("3") int genericFilterColumnsCount,
            @DefaultValue("SHIFT-ENTER") String filterApplyShortcut,
            @DefaultValue("SHIFT-BACKSPACE") String filterSelectShortcut,
            @DefaultValue("top") String genericFilterConditionsLocation,
            @DefaultValue("[filters_popup] [add_condition] [spacer] [settings | save, save_with_values, save_as, edit, remove, make_default, pin, save_search_folder, save_app_folder, clear_values] [max_results] [fts_switch]") String genericFilterControlsLayout,
            @DefaultValue("10") int genericFilterPopupListSize,
            boolean genericFilterChecking,
            @DefaultValue("2") int genericFilterPropertiesHierarchyDepth,
            @DefaultValue("true") boolean genericFilterTrimParamValues,
            @DefaultValue("NULL, 20, 50, 100, 500, 1000, 5000") String genericFilterMaxResultsOptions,
            @DefaultValue("true") boolean genericFilterApplyImmediately,
            @DefaultValue("true") boolean localeSelectVisible,
            @DefaultValue("false") boolean showFolderIcons,
            @DefaultValue("false") boolean foldersPaneVisibleByDefault,
            @DefaultValue("false") boolean foldersPaneEnabled,
            @DefaultValue("180") int appFoldersRefreshPeriodSec,
            @Nullable String appFolderEditWindow,
            @Nullable String folderEditWindow,
            @DefaultValue("200") int foldersPaneDefaultWidth,
            @DefaultValue("true") boolean rememberMeEnabled,
            @DefaultValue("true") boolean poweredByLinkVisible,
            String embeddedResourcesRoot
    ) {
        this.dataManagerBeanValidation = dataManagerBeanValidation;
        this.disableEscapingLikeForDataStores = disableEscapingLikeForDataStores;
        this.allowQueryFromSelected = allowQueryFromSelected;
        this.genericFilterManualApplyRequired = genericFilterManualApplyRequired;
        this.genericFilterColumnsCount = genericFilterColumnsCount;
        this.filterApplyShortcut = filterApplyShortcut;
        this.filterSelectShortcut = filterSelectShortcut;
        this.genericFilterConditionsLocation = genericFilterConditionsLocation;
        this.genericFilterControlsLayout = genericFilterControlsLayout;
        this.genericFilterPopupListSize = genericFilterPopupListSize;
        this.genericFilterChecking = genericFilterChecking;
        this.genericFilterPropertiesHierarchyDepth = genericFilterPropertiesHierarchyDepth;
        this.genericFilterTrimParamValues = genericFilterTrimParamValues;
        this.genericFilterMaxResultsOptions = genericFilterMaxResultsOptions;
        this.genericFilterApplyImmediately = genericFilterApplyImmediately;
        this.localeSelectVisible = localeSelectVisible;
        this.showFolderIcons = showFolderIcons;
        this.foldersPaneVisibleByDefault = foldersPaneVisibleByDefault;
        this.foldersPaneEnabled = foldersPaneEnabled;
        this.appFoldersRefreshPeriodSec = appFoldersRefreshPeriodSec;
        this.appFolderEditWindow = appFolderEditWindow;
        this.folderEditWindow = folderEditWindow;
        this.foldersPaneDefaultWidth = foldersPaneDefaultWidth;
        this.rememberMeEnabled = rememberMeEnabled;
        this.poweredByLinkVisible = poweredByLinkVisible;
        this.embeddedResourcesRoot = embeddedResourcesRoot;
    }

    public boolean isDataManagerBeanValidation() {
        return dataManagerBeanValidation;
    }

    public List<String> getDisableEscapingLikeForDataStores() {
        return disableEscapingLikeForDataStores;
    }

    public boolean isAllowQueryFromSelected() {
        return allowQueryFromSelected;
    }

    public boolean isGenericFilterManualApplyRequired() {
        return genericFilterManualApplyRequired;
    }

    public int getGenericFilterColumnsCount() {
        return genericFilterColumnsCount;
    }

    public String getFilterApplyShortcut() {
        return filterApplyShortcut;
    }

    public String getFilterSelectShortcut() {
        return filterSelectShortcut;
    }

    public String getGenericFilterConditionsLocation() {
        return genericFilterConditionsLocation;
    }

    /**
     * A template for filter controls layout. Each component has the following format:
     * [<i>component_name</i> | <i>options-comma-separated</i>], e.g. [pin | no-caption, no-icon].
     * <p>Available component names:</p>
     * <ul>
     *     <li>{@code filters_popup} - popup button for selecting a filter entity combined with Search button.
     *     When using this component there is no need to add a separate Search button</li>
     *     <li>{@code filters_lookup} - lookup field for selecting a filter entity. Search button should be added as
     *     a separate component</li>
     *     <li>{@code search} - search button. Do not add if use {@code filters_popup}</li>
     *     <li>{@code add_condition} - button for adding a new condition</li>
     *     <li>{@code spacer} - space between component groups </li>
     *     <li>{@code settings} - settings button. Specify actions names that should be displayed in Settings popup
     *     as component options </li>
     *     <li>{@code max_results} - group of components for setting max number of records to be displayed</li>
     *     <li>{@code fts_switch} - checkbox for switching to FTS mode</li>
     * </ul>
     * The following components can be used as options for {@code settings} component. They also can be used as
     * independent components if for example you want to display a Pin button:
     * <ul>
     *     <li>{@code save}</li>
     *     <li>{@code save_as}</li>
     *     <li>{@code edit}</li>
     *     <li>{@code remove}</li>
     *     <li>{@code pin}</li>
     *     <li>{@code make_default}</li>
     *     <li>{@code save_search_folder}</li>
     *     <li>{@code save_app_folder}</li>
     * </ul>
     * Action components can have the following options:
     * <ul>
     *     <li>{@code no-icon} - if an icon shouldn't be displayed on action button. For example: [save | no-icon]</li>
     *     <li>{@code no-caption} - if a caption shouldn't be displayed on action button. For example: [pin | no-caption]</li>
     * </ul>
     */
    public String getGenericFilterControlsLayout() {
        return genericFilterControlsLayout;
    }

    public int getGenericFilterPopupListSize() {
        return genericFilterPopupListSize;
    }

    public boolean isGenericFilterChecking() {
        return genericFilterChecking;
    }

    public int getGenericFilterPropertiesHierarchyDepth() {
        return genericFilterPropertiesHierarchyDepth;
    }

    public boolean isGenericFilterTrimParamValues() {
        return genericFilterTrimParamValues;
    }

    public String getGenericFilterMaxResultsOptions() {
        return genericFilterMaxResultsOptions;
    }

    public boolean isGenericFilterApplyImmediately() {
        return genericFilterApplyImmediately;
    }

    public boolean isLocaleSelectVisible() {
        return localeSelectVisible;
    }

    public boolean isShowFolderIcons() {
        return showFolderIcons;
    }

    public boolean isFoldersPaneVisibleByDefault() {
        return foldersPaneVisibleByDefault;
    }

    public boolean isFoldersPaneEnabled() {
        return foldersPaneEnabled;
    }

    public int getAppFoldersRefreshPeriodSec() {
        return appFoldersRefreshPeriodSec;
    }

    public String getAppFolderEditWindowClassName() {
        return appFolderEditWindow;
    }

    public String getFolderEditWindowClassName() {
        return folderEditWindow;
    }

    public int getFoldersPaneDefaultWidth() {
        return foldersPaneDefaultWidth;
    }

    public boolean isRememberMeEnabled() {
        return rememberMeEnabled;
    }

    public boolean isPoweredByLinkVisible() {
        return poweredByLinkVisible;
    }

    @Nullable
    public String getEmbeddedResourcesRoot() {
        return embeddedResourcesRoot;
    }
}
