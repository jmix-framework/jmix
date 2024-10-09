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

/**
 * INTERNAL.
 */
public class DomDayCellBottomText implements Serializable {

    protected String date;

    protected Integer dow;

    protected boolean isDisabled;

    protected boolean isFuture;

    protected boolean isOther;

    protected boolean isPast;

    protected boolean isToday;

    protected DomViewInfo view;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getDow() {
        return dow;
    }

    public void setDow(Integer dow) {
        this.dow = dow;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public boolean isFuture() {
        return isFuture;
    }

    public void setIsFuture(boolean future) {
        isFuture = future;
    }

    public boolean isOther() {
        return isOther;
    }

    public void setIsOther(boolean other) {
        isOther = other;
    }

    public boolean isPast() {
        return isPast;
    }

    public void setIsPast(boolean past) {
        isPast = past;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setIsToday(boolean today) {
        isToday = today;
    }

    public DomViewInfo getView() {
        return view;
    }

    public void setView(DomViewInfo view) {
        this.view = view;
    }
}
