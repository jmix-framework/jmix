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

package io.jmix.ui;

import io.jmix.ui.app.filter.condition.AddConditionScreen;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Filter;
import io.jmix.ui.sanitizer.HtmlSanitizer;
import io.jmix.ui.widget.JmixMainTabSheet;
import io.jmix.ui.widget.JmixManagedTabSheet;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.ui.component")
@ConstructorBinding
public class UiComponentProperties {

    /**
     * Whether to sanitize the value of components using {@link HtmlSanitizer} to prevent Cross-site Scripting (XSS) in
     * HTML context.
     */
    boolean htmlSanitizerEnabled;

    int uploadFieldMaxUploadSizeMb;

    String tableInsertShortcut;
    String tableAddShortcut;
    String tableRemoveShortcut;
    String tableEditShortcut;
    String tableViewShortcut;

    /**
     * Page length for Table implementation - count of rows for first rendering of Table. After first partial rendering
     * Table will request rest of rows from the server. Setting page length 0 disables paging. If Table has fixed height
     * the client side may update the page length automatically the correct value.
     */
    int tablePageLength;

    /**
     * Whether to enable automatic sorting on grouping columns when collection of grouping columns
     * is changed (reorder, add or remove grouping columns)
     */
    boolean groupTableSortOnGroupEnabled;

    /**
     * This property adjusts a possible caching mechanism of table implementation.
     * <br>
     * Table component may fetch and render some rows outside visible area. With complex tables (for example containing
     * layouts and components), the client side may become unresponsive. Setting the value lower, UI will become more
     * responsive. With higher values scrolling in client will hit server less frequently.
     * <br>
     * The amount of cached rows will be cacheRate multiplied with pageLength {@link #getTablePageLength()} both below
     * and above visible area.
     */
    double tableCacheRate;

    String pickerShortcutModifiers;
    String pickerLookupShortcut;
    String pickerOpenShortcut;
    String pickerClearShortcut;

    /**
     * Page length for the suggestion popup of {@link ComboBox} component. Setting the page length to {@code 0} will
     * disable suggestion popup paging (all items visible).
     */
    int comboBoxPageLength;

    String mainTabSheetNextTabShortcut;
    String mainTabSheetPreviousTabShortcut;

    /**
     * Whether default {@link JmixMainTabSheet} or {@link JmixManagedTabSheet} will be used in AppWorkArea.
     */
    MainTabSheetMode mainTabSheetMode;

    /**
     * How the managed main TabSheet switches its tabs: hides or unloads them.
     */
    ManagedMainTabSheetMode managedMainTabSheetMode;

    /**
     * Options for rows per page ComboBox.
     */
    List<Integer> paginationItemsPerPageOptions;

    Map<String, String> entityFieldType;
    Map<String, List<String>> entityFieldActions;

    /**
     * Default value for the autoApply attribute of the {@link Filter} component
     */
    boolean filterAutoApply;

    /**
     * Shortcut for applying {@link Filter}
     */
    String filterApplyShortcut;

    /**
     * Number of nested properties in the {@link AddConditionScreen}. I.e. if the depth is 2, then you'll be able to
     * select a property "contractor.city.country", if the value is 3, then "contractor.city.country.name", etc.
     */
    int filterPropertiesHierarchyDepth;

    /**
     * Number of columns to be displayed on one row in {@link Filter}.
     */
    int filterColumnsCount;

    /**
     * Whether field for filter configuration id should be visible in the filter configuration edit screens.
     */
    boolean filterShowConfigurationIdField;

    /**
     * Whether validation of filter configuration name uniqueness should be enabled
     */
    boolean filterConfigurationUniqueNames;

    public UiComponentProperties(
            @DefaultValue("true") boolean htmlSanitizerEnabled,
            @DefaultValue("20") int uploadFieldMaxUploadSizeMb,
            @DefaultValue("CTRL-BACKSLASH") String tableInsertShortcut,
            @DefaultValue("CTRL-ALT-BACKSLASH") String tableAddShortcut,
            @DefaultValue("CTRL-DELETE") String tableRemoveShortcut,
            @DefaultValue("ENTER") String tableEditShortcut,
            @DefaultValue("ENTER") String tableViewShortcut,
            @DefaultValue("15") int tablePageLength,
            @DefaultValue("2") double tableCacheRate,
            @DefaultValue("true") boolean groupTableSortOnGroupEnabled,
            @DefaultValue("CTRL-ALT") String pickerShortcutModifiers,
            @DefaultValue("CTRL-ALT-L") String pickerLookupShortcut,
            @DefaultValue("CTRL-ALT-O") String pickerOpenShortcut,
            @DefaultValue("CTRL-ALT-C") String pickerClearShortcut,
            @DefaultValue("10") int comboBoxPageLength,
            @DefaultValue("CTRL-SHIFT-PAGE_DOWN") String mainTabSheetNextTabShortcut,
            @DefaultValue("CTRL-SHIFT-PAGE_UP") String mainTabSheetPreviousTabShortcut,
            @DefaultValue("DEFAULT") MainTabSheetMode mainTabSheetMode,
            @DefaultValue("HIDE_TABS") ManagedMainTabSheetMode managedMainTabSheetMode,
            @DefaultValue({"20", "50", "100", "500", "1000", "5000"}) List<Integer> paginationItemsPerPageOptions,
            @Nullable Map<String, String> entityFieldType,
            @Nullable Map<String, List<String>> entityFieldActions,
            @DefaultValue("true") boolean filterAutoApply,
            @DefaultValue("SHIFT-ENTER") String filterApplyShortcut,
            @DefaultValue("2") int filterPropertiesHierarchyDepth,
            @DefaultValue("3") int filterColumnsCount,
            @DefaultValue("false") boolean filterShowConfigurationIdField,
            @DefaultValue("true") boolean filterConfigurationUniqueNames
    ) {
        this.htmlSanitizerEnabled = htmlSanitizerEnabled;
        this.uploadFieldMaxUploadSizeMb = uploadFieldMaxUploadSizeMb;
        this.tableInsertShortcut = tableInsertShortcut;
        this.tableAddShortcut = tableAddShortcut;
        this.tableRemoveShortcut = tableRemoveShortcut;
        this.tableEditShortcut = tableEditShortcut;
        this.tableViewShortcut = tableViewShortcut;
        this.tablePageLength = tablePageLength;
        this.tableCacheRate = tableCacheRate;
        this.groupTableSortOnGroupEnabled = groupTableSortOnGroupEnabled;
        this.pickerShortcutModifiers = pickerShortcutModifiers;
        this.pickerLookupShortcut = pickerLookupShortcut;
        this.pickerOpenShortcut = pickerOpenShortcut;
        this.pickerClearShortcut = pickerClearShortcut;
        this.comboBoxPageLength = comboBoxPageLength;
        this.mainTabSheetNextTabShortcut = mainTabSheetNextTabShortcut;
        this.mainTabSheetPreviousTabShortcut = mainTabSheetPreviousTabShortcut;
        this.mainTabSheetMode = mainTabSheetMode;
        this.managedMainTabSheetMode = managedMainTabSheetMode;
        this.paginationItemsPerPageOptions = paginationItemsPerPageOptions;
        this.entityFieldType = entityFieldType == null ? Collections.emptyMap() : entityFieldType;
        this.entityFieldActions = entityFieldActions == null ? Collections.emptyMap() : entityFieldActions;
        this.filterAutoApply = filterAutoApply;
        this.filterApplyShortcut = filterApplyShortcut;
        this.filterPropertiesHierarchyDepth = filterPropertiesHierarchyDepth;
        this.filterColumnsCount = filterColumnsCount;
        this.filterShowConfigurationIdField = filterShowConfigurationIdField;
        this.filterConfigurationUniqueNames = filterConfigurationUniqueNames;
    }

    public int getUploadFieldMaxUploadSizeMb() {
        return uploadFieldMaxUploadSizeMb;
    }

    public String getTableInsertShortcut() {
        return tableInsertShortcut;
    }

    public String getTableAddShortcut() {
        return tableAddShortcut;
    }

    public String getTableRemoveShortcut() {
        return tableRemoveShortcut;
    }

    public String getTableEditShortcut() {
        return tableEditShortcut;
    }

    public String getTableViewShortcut() {
        return tableViewShortcut;
    }

    public String getMainTabSheetNextTabShortcut() {
        return mainTabSheetNextTabShortcut;
    }

    public String getMainTabSheetPreviousTabShortcut() {
        return mainTabSheetPreviousTabShortcut;
    }

    public String getPickerShortcutModifiers() {
        return pickerShortcutModifiers;
    }

    public String getPickerLookupShortcut() {
        return pickerLookupShortcut;
    }

    public String getPickerOpenShortcut() {
        return pickerOpenShortcut;
    }

    public String getPickerClearShortcut() {
        return pickerClearShortcut;
    }

    /**
     * @see #comboBoxPageLength
     */
    public int getComboBoxPageLength() {
        return comboBoxPageLength;
    }

    /**
     * @see #tablePageLength
     */
    public int getTablePageLength() {
        return tablePageLength;
    }

    /**
     * @see #tableCacheRate
     */
    public double getTableCacheRate() {
        return tableCacheRate;
    }

    /**
     * @see #groupTableSortOnGroupEnabled
     */
    public boolean isGroupTableSortOnGroupEnabled() {
        return groupTableSortOnGroupEnabled;
    }

    /**
     * @see #mainTabSheetMode
     */
    public MainTabSheetMode getMainTabSheetMode() {
        return mainTabSheetMode;
    }

    /**
     * @see #managedMainTabSheetMode
     */
    public ManagedMainTabSheetMode getManagedMainTabSheetMode() {
        return managedMainTabSheetMode;
    }

    /**
     * @see #htmlSanitizerEnabled
     */
    public boolean isHtmlSanitizerEnabled() {
        return htmlSanitizerEnabled;
    }

    /**
     * @see #paginationItemsPerPageOptions
     */
    public List<Integer> getPaginationItemsPerPageOptions() {
        return paginationItemsPerPageOptions;
    }

    public Map<String, String> getEntityFieldType() {
        return entityFieldType;
    }

    public Map<String, List<String>> getEntityFieldActions() {
        return entityFieldActions;
    }

    /**
     * @see #filterAutoApply
     */
    public boolean isFilterAutoApply() {
        return filterAutoApply;
    }

    /**
     * @see #filterApplyShortcut
     */
    public String getFilterApplyShortcut() {
        return filterApplyShortcut;
    }

    /**
     * @see #filterPropertiesHierarchyDepth
     */
    public int getFilterPropertiesHierarchyDepth() {
        return filterPropertiesHierarchyDepth;
    }

    /**
     * @see #filterColumnsCount
     */
    public int getFilterColumnsCount() {
        return filterColumnsCount;
    }

    /**
     * @see #filterShowConfigurationIdField
     */
    public boolean isFilterShowConfigurationIdField() {
        return filterShowConfigurationIdField;
    }

    /**
     * @see #filterConfigurationUniqueNames
     */
    public boolean isFilterConfigurationUniqueNames() {
        return filterConfigurationUniqueNames;
    }
}
