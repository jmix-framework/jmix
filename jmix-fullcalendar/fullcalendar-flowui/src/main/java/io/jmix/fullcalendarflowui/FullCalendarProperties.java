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

package io.jmix.fullcalendarflowui;

import io.jmix.fullcalendarflowui.action.DaysOfWeekEditAction;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jmix.ui.fullcalendar")
public class FullCalendarProperties {

    /**
     * The shortcut triggers the execution of the {@link DaysOfWeekEditAction}. This applies to all actions within
     * the application.
     */
    String pickerDaysOfWeekEditShortcut;

    public FullCalendarProperties(String pickerDaysOfWeekEditShortcut) {
        this.pickerDaysOfWeekEditShortcut = pickerDaysOfWeekEditShortcut;
    }

    /**
     * See {@link #pickerDaysOfWeekEditShortcut}.
     *
     * @return the shortcut for triggering {@link DaysOfWeekEditAction}
     */
    public String getPickerDaysOfWeekEditShortcut() {
        return pickerDaysOfWeekEditShortcut;
    }
}
