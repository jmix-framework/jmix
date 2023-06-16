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
import io.jmix.flowui.component.genericfilter.GenericFilter;
import io.jmix.flowui.component.genericfilter.configuration.FilterConfigurationDetail;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.ui.component")
public class UiComponentProperties {

    String gridCreateShortcut;
    String gridAddShortcut;
    String gridRemoveShortcut;
    String gridEditShortcut;
    String gridReadShortcut;

    String defaultNotificationPosition;
    int defaultNotificationDuration;

    String pickerLookupShortcut;
    String pickerOpenShortcut;
    String pickerClearShortcut;

    /**
     * Items for rows per page component.
     */
    List<Integer> paginationItemsPerPageItems;

    /**
     * Default value for the autoApply attribute of the {@link GenericFilter} component
     */
    boolean filterAutoApply;

    /**
     * Number of nested properties in the {@link AddConditionView}. I.e. if the depth is 2, then you'll be able to
     * select a property "contractor.city.country", if the value is 3, then "contractor.city.country.name", etc.
     */
    int filterPropertiesHierarchyDepth;

    /**
     * Whether field for filter configuration id should be visible in the {@link FilterConfigurationDetail}.
     */
    boolean filterShowConfigurationIdField;

    public UiComponentProperties(
            String gridCreateShortcut,
            String gridAddShortcut,
            String gridRemoveShortcut,
            @DefaultValue("ENTER") String gridEditShortcut,
            @DefaultValue("ENTER") String gridReadShortcut,
            @DefaultValue("MIDDLE") String defaultNotificationPosition,
            @DefaultValue("3000") int defaultNotificationDuration,
            String pickerLookupShortcut,
            String pickerOpenShortcut,
            String pickerClearShortcut,
            @DefaultValue({"20", "50", "100", "500", "1000", "5000"}) List<Integer> paginationItemsPerPageItems,
            @DefaultValue("true") boolean filterAutoApply,
            @DefaultValue("2") int filterPropertiesHierarchyDepth,
            @DefaultValue("false") boolean filterShowConfigurationIdField) {
        this.gridCreateShortcut = gridCreateShortcut;
        this.gridAddShortcut = gridAddShortcut;
        this.gridRemoveShortcut = gridRemoveShortcut;
        this.gridEditShortcut = gridEditShortcut;
        this.gridReadShortcut = gridReadShortcut;
        this.defaultNotificationPosition = defaultNotificationPosition;
        this.defaultNotificationDuration = defaultNotificationDuration;

        this.pickerLookupShortcut = pickerLookupShortcut;
        this.pickerOpenShortcut = pickerOpenShortcut;
        this.pickerClearShortcut = pickerClearShortcut;

        this.paginationItemsPerPageItems = paginationItemsPerPageItems;

        this.filterAutoApply = filterAutoApply;
        this.filterPropertiesHierarchyDepth = filterPropertiesHierarchyDepth;
        this.filterShowConfigurationIdField = filterShowConfigurationIdField;
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

    public Notification.Position getDefaultNotificationPosition() {
        return Notification.Position.valueOf(defaultNotificationPosition);
    }

    public int getDefaultNotificationDuration() {
        return defaultNotificationDuration;
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
     * @see #filterAutoApply
     */
    public boolean isFilterAutoApply() {
        return filterAutoApply;
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
}
