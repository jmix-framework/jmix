/*
 * Copyright 2020 Haulmont.
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

package io.jmix.ui.widget.client.calendar;

import io.jmix.ui.widget.client.calendar.schedule.JmixSimpleDayCell;
import com.vaadin.v7.client.ui.VCalendar;
import com.vaadin.v7.client.ui.calendar.schedule.SimpleDayCell;
import io.jmix.ui.widget.client.calendar.schedule.JmixWeekGrid;

import java.util.Date;
import java.util.function.Consumer;

public class JmixCalendarWidget extends VCalendar {

    protected Consumer<Date> dayClickListener;

    /*
     * We must also handle the special case when the event lasts exactly for 24
     * hours, thus spanning two days e.g. from 1.1.2001 00:00 to 2.1.2001 00:00.
     * That special case still should span one day when rendered.
     */
    @SuppressWarnings("deprecation")
    // Date methods are not deprecated in GWT
    @Override
    protected boolean isEventInDayWithTime(Date from, Date to, Date date, Date endTime, boolean isAllDay) {
        return (isAllDay || !(to.compareTo(date) == 0
                && from.compareTo(to) != 0 && isMidnight(endTime)));
    }

    @Override
    protected SimpleDayCell createSimpleDayCell(int y, int x) {
        return new JmixSimpleDayCell(this, y, x);
    }

    public Consumer<Date> getDayClickListener() {
        return dayClickListener;
    }

    public void setDayClickListener(Consumer<Date> dayClickListener) {
        this.dayClickListener = dayClickListener;
    }

    @Override
    protected void createWeekGrid() {
        if (weekGrid == null) {
            weekGrid = new JmixWeekGrid(this, is24HFormat());
        }
    }
}
