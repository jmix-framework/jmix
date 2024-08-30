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

package io.jmix.fullcalendarflowui.kit.component.model;

import jakarta.annotation.Nullable;

public abstract class AbstractTimeGridViewProperties extends AbstractCalendarViewProperties {

    protected String dayPopoverFormat;

    protected String dayHeaderFormat;

    protected String weekNumberFormat;

    protected String eventTimeFormat;

    protected String slotLabelFormat;

    protected Integer eventMinHeight;

    protected Integer eventShortHeight;

    protected boolean slotEventOverlap = true;

    protected boolean allDaySlot = true;

    protected boolean displayEventEnd = true;

    public AbstractTimeGridViewProperties(String name) {
        super(name);
    }

    @Nullable
    public String getDayPopoverFormat() {
        return dayPopoverFormat;
    }

    public void setDayPopoverFormat(@Nullable String dayPopoverFormat) {
        this.dayPopoverFormat = dayPopoverFormat;

        markAsDirty();
    }

    @Nullable
    public String getDayHeaderFormat() {
        return dayHeaderFormat;
    }

    public void setDayHeaderFormat(@Nullable String dayHeaderFormat) {
        this.dayHeaderFormat = dayHeaderFormat;
    }

    @Nullable
    public String getWeekNumberFormat() {
        return weekNumberFormat;
    }

    public void setWeekNumberFormat(@Nullable String weekNumberFormat) {
        this.weekNumberFormat = weekNumberFormat;

        markAsDirty();
    }

    @Nullable
    public String getSlotLabelFormat() {
        return slotLabelFormat;
    }

    public void setSlotLabelFormat(@Nullable String slotLabelFormat) {
        this.slotLabelFormat = slotLabelFormat;

        markAsDirty();
    }

    @Nullable
    public String getEventTimeFormat() {
        return eventTimeFormat;
    }

    public void setEventTimeFormat(@Nullable String eventTimeFormat) {
        this.eventTimeFormat = eventTimeFormat;

        markAsDirty();
    }

    @Nullable
    public Integer getEventMinHeight() {
        return eventMinHeight;
    }

    public void setEventMinHeight(@Nullable Integer eventMinHeight) {
        this.eventMinHeight = eventMinHeight;

        markAsDirty();
    }

    @Nullable
    public Integer getEventShortHeight() {
        return eventShortHeight;
    }

    public void setEventShortHeight(@Nullable Integer eventShortHeight) {
        this.eventShortHeight = eventShortHeight;

        markAsDirty();
    }

    public boolean isSlotEventOverlap() {
        return slotEventOverlap;
    }

    public void setSlotEventOverlap(boolean slotEventOverlap) {
        this.slotEventOverlap = slotEventOverlap;

        markAsDirty();
    }

    public boolean isAllDaySlot() {
        return allDaySlot;
    }

    public void setAllDaySlot(boolean allDaySlot) {
        this.allDaySlot = allDaySlot;

        markAsDirty();
    }

    public boolean isDisplayEventEnd() {
        return displayEventEnd;
    }

    public void setDisplayEventEnd(boolean displayEventEnd) {
        this.displayEventEnd = displayEventEnd;

        markAsDirty();
    }
}
