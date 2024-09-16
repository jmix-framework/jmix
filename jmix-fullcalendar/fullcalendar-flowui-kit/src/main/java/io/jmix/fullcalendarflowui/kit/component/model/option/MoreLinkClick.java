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

import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayMode;
import jakarta.annotation.Nullable;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionConstants.MORE_LINK_CLICK;

/**
 * INTERNAL.
 */
public class MoreLinkClick extends CalendarOption {

    protected CalendarDisplayMode calendarView;

    protected boolean functionEnabled = false;

    public MoreLinkClick() {
        super(MORE_LINK_CLICK);
    }

    @Nullable
    public CalendarDisplayMode getCalendarView() {
        return calendarView ;
    }

    public void setCalendarView(@Nullable CalendarDisplayMode calendarView) {
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
