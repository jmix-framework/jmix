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

public class MultiMonthYearProperties extends AbstractCalendarViewProperties {

    protected Integer multiMonthMaxColumns;

    protected Integer multiMonthMinWidth;

    protected String multiMonthTitleFormat;

    protected boolean fixedWeekCount = true;

    protected boolean showNonCurrentDates = true;

    public MultiMonthYearProperties() {
        super(CalendarViewType.MULTI_MONTH_YEAR.getId());
    }

    @Nullable
    public Integer getMultiMonthMaxColumns() {
        return multiMonthMaxColumns;
    }

    public void setMultiMonthMaxColumns(@Nullable Integer multiMonthMaxColumns) {
        this.multiMonthMaxColumns = multiMonthMaxColumns;

        markAsDirty();
    }

    @Nullable
    public Integer getMultiMonthMinWidth() {
        return multiMonthMinWidth;
    }

    public void setMultiMonthMinWidth(@Nullable Integer multiMonthMinWidth) {
        this.multiMonthMinWidth = multiMonthMinWidth;

        markAsDirty();
    }

    @Nullable
    public String getMultiMonthTitleFormat() {
        return multiMonthTitleFormat;
    }

    public void setMultiMonthTitleFormat(@Nullable String multiMonthTitleFormat) {
        this.multiMonthTitleFormat = multiMonthTitleFormat;

        markAsDirty();
    }

    public boolean isFixedWeekCount() {
        return fixedWeekCount;
    }

    public void setFixedWeekCount(boolean fixedWeekCount) {
        this.fixedWeekCount = fixedWeekCount;

        markAsDirty();
    }

    public boolean isShowNonCurrentDates() {
        return showNonCurrentDates;
    }

    public void setShowNonCurrentDates(boolean showNonCurrentDates) {
        this.showNonCurrentDates = showNonCurrentDates;

        markAsDirty();
    }
}
