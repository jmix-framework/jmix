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
import io.jmix.fullcalendarflowui.kit.component.model.option.CalendarOption;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

public class BusinessHoursOption extends CalendarOption {
    public static final String NAME = "businessHours";

    protected boolean enabled = false;

    protected List<BusinessHours> businessHours;

    public BusinessHoursOption() {
        super(NAME);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        markAsDirty();
    }

    public List<BusinessHours> getBusinessHours() {
        return businessHours == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(businessHours);
    }

    public void setBusinessHours(@Nullable List<BusinessHours> businessHours) {
        this.businessHours = businessHours;

        markAsDirty();
    }
}