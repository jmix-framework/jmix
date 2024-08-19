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

import io.jmix.fullcalendarflowui.kit.component.model.option.JmixFullCalendarOptions;
import io.jmix.fullcalendarflowui.kit.component.model.option.OptionUtils;

import java.util.List;

public class FullCalendarOptions extends JmixFullCalendarOptions {

    protected EventConstraint eventConstraint = new EventConstraint();
    protected BusinessHoursOption businessHours = new BusinessHoursOption();
    protected SelectConstraint selectConstraint = new SelectConstraint();

    public FullCalendarOptions() {
        options.addAll(List.of(eventConstraint, businessHours, selectConstraint));

        OptionUtils.addChangeListener(eventConstraint, this::onOptionChange);
        OptionUtils.addChangeListener(businessHours, this::onOptionChange);
        OptionUtils.addChangeListener(selectConstraint, this::onOptionChange);
    }

    public EventConstraint getEventConstraint() {
        return eventConstraint;
    }

    public BusinessHoursOption getBusinessHours() {
        return businessHours;
    }

    public SelectConstraint getSelectConstraint() {
        return selectConstraint;
    }
}
