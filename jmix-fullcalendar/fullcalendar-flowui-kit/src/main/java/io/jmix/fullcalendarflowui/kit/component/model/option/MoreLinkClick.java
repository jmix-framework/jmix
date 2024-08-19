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

package io.jmix.fullcalendarflowui.kit.component.model.option;

import io.jmix.fullcalendarflowui.kit.component.model.CalendarView;
import jakarta.annotation.Nullable;

public class MoreLinkClick extends CalendarOption {
    public static final String NAME = "moreLinkClick";

    protected CalendarView calendarView;

    protected boolean functionEnabled = false;

    public MoreLinkClick() {
        super(NAME);
    }

    @Nullable
    public CalendarView getCalendarView() {
        return calendarView ;
    }

    public void setCalendarView(@Nullable CalendarView calendarView) {
        this.calendarView = calendarView;

        markAsDirty();
    }

    public boolean isFunctionEnabled() {
        return functionEnabled;
    }

    public void setFunctionEnabled(boolean functionEnabled) {
        this.functionEnabled = functionEnabled;

        markAsDirty();
    }
}
