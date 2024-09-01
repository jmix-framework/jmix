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

package io.jmix.fullcalendarflowui.kit.component.model.dom;

import java.io.Serializable;
import java.util.List;

/**
 * INTERNAL.
 */
public class DomMoreLinkClick implements Serializable {

    protected boolean allDay;

    protected String dateTime;

    protected DomViewInfo view;

    protected DomMouseEventDetails mouseDetails;

    protected List<DomSegment> allData;

    protected List<DomSegment> hiddenData;

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public DomViewInfo getView() {
        return view;
    }

    public void setView(DomViewInfo view) {
        this.view = view;
    }

    public DomMouseEventDetails getMouseDetails() {
        return mouseDetails;
    }

    public void setMouseDetails(DomMouseEventDetails mouseDetails) {
        this.mouseDetails = mouseDetails;
    }

    public List<DomSegment> getAllData() {
        return allData;
    }

    public void setAllData(List<DomSegment> allData) {
        this.allData = allData;
    }

    public List<DomSegment> getHiddenData() {
        return hiddenData;
    }

    public void setHiddenData(List<DomSegment> hiddenData) {
        this.hiddenData = hiddenData;
    }
}
