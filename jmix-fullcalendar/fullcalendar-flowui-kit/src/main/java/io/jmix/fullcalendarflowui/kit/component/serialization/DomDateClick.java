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

package io.jmix.fullcalendarflowui.kit.component.serialization;

import java.io.Serializable;

public class DomDateClick implements Serializable {

    protected String dateTime;

    protected boolean allDay;

    protected DomMouseEventDetails mouseDetails;

    protected DomViewInfo view;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public DomMouseEventDetails getMouseDetails() {
        return mouseDetails;
    }

    public void setMouseDetails(DomMouseEventDetails mouseDetails) {
        this.mouseDetails = mouseDetails;
    }

    public DomViewInfo getView() {
        return view;
    }

    public void setView(DomViewInfo view) {
        this.view = view;
    }
}
