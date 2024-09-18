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

package io.jmix.fullcalendarflowui.component.model.option;

import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.Display;
import io.jmix.fullcalendarflowui.kit.component.model.option.JmixFullCalendarOptions;
import io.jmix.fullcalendarflowui.kit.component.model.option.SimpleOption;

import java.util.List;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils.*;

/**
 * INTERNAL.
 */
public class FullCalendarOptions extends JmixFullCalendarOptions {

    @Override
    protected void addAdditionalOptions() {
        optionsMap.put(BUSINESS_HOURS, new BusinessHours());
        optionsMap.put(EVENT_CONSTRAINT, new EventConstraint());
        optionsMap.put(EVENT_DISPLAY, new SimpleOption<>(EVENT_DISPLAY, Display.AUTO));
        optionsMap.put(HIDDEN_DAYS, new SimpleOption<>(HIDDEN_DAYS));
        optionsMap.put(SELECT_CONSTRAINT, new SelectConstraint());
    }

    public EventConstraint getEventConstraint() {
        return get(EVENT_CONSTRAINT);
    }

    public BusinessHours getBusinessHours() {
        return get(BUSINESS_HOURS);
    }

    public SelectConstraint getSelectConstraint() {
        return get(SELECT_CONSTRAINT);
    }

    public SimpleOption<List<DayOfWeek>> getHiddenDays() {
        return get(HIDDEN_DAYS);
    }

    public SimpleOption<Display> getEventDisplay() {
        return get(EVENT_DISPLAY);
    }
}
