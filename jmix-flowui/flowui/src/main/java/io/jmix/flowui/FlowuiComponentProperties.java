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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "jmix.flowui.component")
@ConstructorBinding
public class FlowuiComponentProperties {

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

    public FlowuiComponentProperties(
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
            @DefaultValue({"20", "50", "100", "500", "1000", "5000"}) List<Integer> paginationItemsPerPageItems) {
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
}
