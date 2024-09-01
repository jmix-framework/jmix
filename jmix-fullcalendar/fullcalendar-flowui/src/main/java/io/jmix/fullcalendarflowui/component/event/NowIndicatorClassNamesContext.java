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

package io.jmix.fullcalendarflowui.component.event;

import io.jmix.fullcalendarflowui.component.FullCalendar;

import java.time.LocalDateTime;

public class NowIndicatorClassNamesContext extends AbstractFullCalendarContext {

    protected final boolean isAxis;

    protected final LocalDateTime dateTime;

    protected final ViewInfo viewInfo;

    public NowIndicatorClassNamesContext(FullCalendar fullCalendar,
                                         boolean isAxis,
                                         LocalDateTime dateTime,
                                         ViewInfo viewInfo) {
        super(fullCalendar);
        this.isAxis = isAxis;
        this.dateTime = dateTime;
        this.viewInfo = viewInfo;
    }

    /**
     * @return {@code true} if class names will be applied for now-indicator's arrow, otherwise class names will be
     * applied for now-indicator's line
     */
    public boolean isAxis() {
        return isAxis;
    }

    /**
     * Returns date-time as is from component without transformation.
     * <p>
     * For now-indicator's line the time part will be {@code 00:00}.
     *
     * @return indicator's date-time
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }
}
