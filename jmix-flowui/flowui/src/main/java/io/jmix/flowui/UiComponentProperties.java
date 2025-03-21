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

package io.jmix.flowui;

import com.vaadin.flow.component.notification.Notification;
import io.jmix.flowui.app.filter.condition.AddConditionView;
import io.jmix.flowui.component.SupportsTrimming;
import io.jmix.flowui.component.checkbox.JmixCheckbox;
import io.jmix.flowui.component.factory.EntityFieldCreationSupport;
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.FilterConfigurationDetail;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.ui.component")
public class UiComponentProperties {

    String gridCreateShortcut;
    String gridAddShortcut;
    String gridRemoveShortcut;
    String gridEditShortcut;
    String gridReadShortcut;

    /**
     * Whether to show action icons in grid context menu
     */
    boolean gridContextMenuShowActionIcons;

    /**
     * Whether to show action shortcuts in grid context menu
     */
    boolean gridContextMenuShowActionShortcuts;

    String defaultNotificationPosition;
    int defaultNotificationDuration;

    /**
     * Whether to fire {@link Notifications} opened and closed events.
     */
    boolean notificationsOpenedChangeEventsEnabled;

    /**
     * Whether to fire {@link Dialogs} opened and closed events.
     */
    boolean dialogsOpenedChangeEventsEnabled;

    String pickerLookupShortcut;
    String pickerOpenShortcut;
    String pickerClearShortcut;

    /**
     * Items for rows per page component.
     */
    List<Integer> paginationItemsPerPageItems;


    /**
     * Map for defining the component to be used for selecting the specified entity in generation strategy mechanisms
     *
     * @see EntityFieldCreationSupport
     */
    Map<String, String> entityFieldFqn;

    /**
     * Map for defining actions to be added to the selection component for the specified entity in generation strategy
     * mechanisms
     *
     * @see EntityFieldCreationSupport
     */
    Map<String, List<String>> entityFieldActions;

    /**
     * Default value for the autoApply attribute of the {@link GenericFilter} component
     */
    boolean filterAutoApply;

    /**
     * Shortcut for applying {@link GenericFilter}
     */
    String filterApplyShortcut;

    /**
     * Number of nested properties in the {@link AddConditionView}. I.e. if the depth is 2, then you'll be able to
     * select a property "contractor.city.country", if the value is 3, then "contractor.city.country.name", etc.
     */
    int filterPropertiesHierarchyDepth;

    /**
     * Whether field for filter configuration id should be visible in the {@link FilterConfigurationDetail}.
     */
    boolean filterShowConfigurationIdField;

    /**
     * Whether non-JPA properties for filter should be visible in the {@link AddConditionView}.
     */
    boolean filterShowNonJpaProperties;

    /**
     * Whether validation of filter configuration name uniqueness should be enabled
     */
    boolean filterConfigurationUniqueNamesEnabled;

    /**
     * Whether error message should be shown below the field or not.
     */
    boolean showErrorMessageBelowField;

    /**
     * Whether error message should be shown immediately after the form is opened.
     */
    boolean immediateRequiredValidationEnabled;

    /**
     * Whether to trim the entered string by default for {@link SupportsTrimming} components.
     */
    boolean defaultTrimEnabled;

    /**
     * Whether the {@link JmixCheckbox} should initialize its required state during data binding based on the
     * {@link NotNull} annotation or the mandatory property.
     */
    boolean checkboxRequiredStateInitializationEnabled;

    public UiComponentProperties(
            String gridCreateShortcut,
            String gridAddShortcut,
            String gridRemoveShortcut,
            @DefaultValue("ENTER") String gridEditShortcut,
            @DefaultValue("ENTER") String gridReadShortcut,
            @DefaultValue("false") boolean gridContextMenuShowActionIcons,
            @DefaultValue("false") boolean gridContextMenuShowActionShortcuts,
            @DefaultValue("MIDDLE") String defaultNotificationPosition,
            @DefaultValue("false") boolean notificationsOpenedChangeEventsEnabled,
            @DefaultValue("false") boolean dialogsOpenedChangeEventsEnabled,
            @DefaultValue("3000") int defaultNotificationDuration,
            String pickerLookupShortcut,
            String pickerOpenShortcut,
            String pickerClearShortcut,
            @DefaultValue({"20", "50", "100", "500", "1000", "5000"}) List<Integer> paginationItemsPerPageItems,
            @Nullable Map<String, String> entityFieldFqn,
            @Nullable Map<String, List<String>> entityFieldActions,
            @DefaultValue("true") boolean filterAutoApply,
            String filterApplyShortcut,
            @DefaultValue("2") int filterPropertiesHierarchyDepth,
            @DefaultValue("false") boolean filterShowConfigurationIdField,
            @DefaultValue("true") boolean filterShowNonJpaProperties,
            @DefaultValue("true") boolean filterConfigurationUniqueNamesEnabled,
            @DefaultValue("true") boolean showErrorMessageBelowField,
            @DefaultValue("true") boolean immediateRequiredValidationEnabled,
            @DefaultValue("true") boolean defaultTrimEnabled,
            @DefaultValue("true") boolean checkboxRequiredStateInitializationEnabled) {
        this.gridCreateShortcut = gridCreateShortcut;
        this.gridAddShortcut = gridAddShortcut;
        this.gridRemoveShortcut = gridRemoveShortcut;
        this.gridEditShortcut = gridEditShortcut;
        this.gridReadShortcut = gridReadShortcut;
        this.gridContextMenuShowActionIcons = gridContextMenuShowActionIcons;
        this.gridContextMenuShowActionShortcuts = gridContextMenuShowActionShortcuts;
        this.defaultNotificationPosition = defaultNotificationPosition;
        this.defaultNotificationDuration = defaultNotificationDuration;

        this.notificationsOpenedChangeEventsEnabled = notificationsOpenedChangeEventsEnabled;
        this.dialogsOpenedChangeEventsEnabled = dialogsOpenedChangeEventsEnabled;

        this.pickerLookupShortcut = pickerLookupShortcut;
        this.pickerOpenShortcut = pickerOpenShortcut;
        this.pickerClearShortcut = pickerClearShortcut;

        this.paginationItemsPerPageItems = paginationItemsPerPageItems;

        this.entityFieldFqn = entityFieldFqn == null ? Collections.emptyMap() : entityFieldFqn;
        this.entityFieldActions = entityFieldActions == null ? Collections.emptyMap() : entityFieldActions;

        this.filterAutoApply = filterAutoApply;
        this.filterApplyShortcut = filterApplyShortcut;
        this.filterPropertiesHierarchyDepth = filterPropertiesHierarchyDepth;
        this.filterShowConfigurationIdField = filterShowConfigurationIdField;
        this.filterShowNonJpaProperties = filterShowNonJpaProperties;
        this.filterConfigurationUniqueNamesEnabled = filterConfigurationUniqueNamesEnabled;

        this.showErrorMessageBelowField = showErrorMessageBelowField;
        this.immediateRequiredValidationEnabled = immediateRequiredValidationEnabled;

        this.defaultTrimEnabled = defaultTrimEnabled;

        this.checkboxRequiredStateInitializationEnabled = checkboxRequiredStateInitializationEnabled;
    }

    public String getGridCreateShortcut() {
        return gridCreateShortcut;
    }

    public String getGridAddShortcut() {
        return gridAddShortcut;
    }

    public String getGridRemoveShortcut() {
        return gridRemoveShortcut;
    }

    public String getGridEditShortcut() {
        return gridEditShortcut;
    }

    public String getGridReadShortcut() {
        return gridReadShortcut;
    }

    /**
     * @see #gridContextMenuShowActionIcons
     */
    public boolean isGridContextMenuShowActionIcons() {
        return gridContextMenuShowActionIcons;
    }

    /**
     * @see #gridContextMenuShowActionShortcuts
     */
    public boolean isGridContextMenuShowActionShortcuts() {
        return gridContextMenuShowActionShortcuts;
    }

    public Notification.Position getDefaultNotificationPosition() {
        return Notification.Position.valueOf(defaultNotificationPosition);
    }

    public int getDefaultNotificationDuration() {
        return defaultNotificationDuration;
    }

    /**
     * @see #notificationsOpenedChangeEventsEnabled
     */
    public boolean isNotificationsOpenedChangeEventsEnabled() {
        return notificationsOpenedChangeEventsEnabled;
    }

    /**
     * @see #dialogsOpenedChangeEventsEnabled
     */
    public boolean isDialogsOpenedChangeEventsEnabled() {
        return dialogsOpenedChangeEventsEnabled;
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
     * @see #paginationItemsPerPageItems
     */
    public List<Integer> getPaginationItemsPerPageItems() {
        return paginationItemsPerPageItems;
    }

    /**
     * @see #entityFieldFqn
     */
    public Map<String, String> getEntityFieldFqn() {
        return entityFieldFqn;
    }

    /**
     * @see #entityFieldActions
     */
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
     * @see #filterShowConfigurationIdField
     */
    public boolean isFilterShowConfigurationIdField() {
        return filterShowConfigurationIdField;
    }

    /**
     * @see #filterShowNonJpaProperties
     */
    public boolean isFilterShowNonJpaProperties() {
        return filterShowNonJpaProperties;
    }

    /**
     * @see #filterConfigurationUniqueNamesEnabled
     */
    public boolean isFilterConfigurationUniqueNamesEnabled() {
        return filterConfigurationUniqueNamesEnabled;
    }

    /**
     * @see #showErrorMessageBelowField
     */
    public boolean isShowErrorMessageBelowField() {
        return showErrorMessageBelowField;
    }

    /**
     * @see #immediateRequiredValidationEnabled
     */
    public boolean isImmediateRequiredValidationEnabled() {
        return immediateRequiredValidationEnabled;
    }

    /**
     * @see #defaultTrimEnabled
     */
    public boolean isDefaultTrimEnabled() {
        return defaultTrimEnabled;
    }

    /**
     * @see #checkboxRequiredStateInitializationEnabled
     */
    public boolean isCheckboxRequiredStateInitializationEnabled() {
        return checkboxRequiredStateInitializationEnabled;
    }
}
