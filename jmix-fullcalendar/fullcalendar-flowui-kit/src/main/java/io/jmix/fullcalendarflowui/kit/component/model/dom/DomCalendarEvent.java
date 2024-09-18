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
import java.util.Map;
import java.util.Objects;

/**
 * INTERNAL.
 */
public class DomCalendarEvent implements Serializable {

    protected String id;

    protected String start;

    protected String end;

    protected boolean allDay;

    protected Map<String, Object> extendedProps;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public Map<String, Object> getExtendedProps() {
        return extendedProps;
    }

    public void setExtendedProps(Map<String, Object> extendedProps) {
        this.extendedProps = extendedProps;
    }

    public String getSourceId() {
        return getExtendedProps().get("jmixSourceId").toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getSourceId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof DomCalendarEvent eObj)
                && Objects.equals(id, eObj.id)
                && Objects.equals(getSourceId(), eObj.getSourceId());
    }
}
