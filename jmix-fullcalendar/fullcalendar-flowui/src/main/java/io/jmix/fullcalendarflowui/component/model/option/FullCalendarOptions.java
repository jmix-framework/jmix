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

import io.jmix.fullcalendarflowui.component.model.BusinessHours;
import io.jmix.fullcalendarflowui.kit.component.model.option.JmixFullCalendarOptions;
import org.springframework.lang.Nullable;

import java.util.List;

public class FullCalendarOptions extends JmixFullCalendarOptions {

    protected Option eventConstraint = new Option("eventConstraint", new EventConstraint());
    protected Option businessHours = new Option("businessHours", new BusinessHoursOption());
    protected Option selectConstraint = new Option("selectConstraint", new SelectConstraint());

    public FullCalendarOptions() {
        options.addAll(List.of(eventConstraint, businessHours, selectConstraint));
    }

    public EventConstraint getEventConstraint() {
        return eventConstraint.getValue();
    }

    public void setEventConstraint(boolean enabled,
                                   @Nullable String groupId,
                                   @Nullable List<BusinessHours> businessHours) {
        eventConstraint.setValue(new EventConstraint(enabled, groupId, businessHours));
    }

    public BusinessHoursOption getBusinessHours() {
        return businessHours.getValue();
    }

    public void setBusinessHours(boolean enabled, @Nullable List<BusinessHours> businessHours) {
        this.businessHours.setValue(new BusinessHoursOption(enabled, businessHours));
    }


    public SelectConstraint getSelectConstraint() {
        return selectConstraint.getValue();
    }

    public void setSelectConstraint(boolean enabled,
                                    @Nullable String groupId,
                                    @Nullable List<BusinessHours> businessHours) {
        selectConstraint.setValue(new SelectConstraint(enabled, groupId, businessHours));
    }
}
