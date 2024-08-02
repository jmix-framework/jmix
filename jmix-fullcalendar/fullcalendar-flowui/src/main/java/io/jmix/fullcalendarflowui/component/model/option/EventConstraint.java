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
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class EventConstraint implements Serializable {

    protected boolean enabled = false;

    protected String groupId;

    protected List<BusinessHours> businessHours;

    public EventConstraint() {
    }

    public EventConstraint(boolean enabled, @Nullable String groupId, @Nullable List<BusinessHours> businessHours) {
        this.enabled = enabled;
        this.groupId = groupId;
        this.businessHours = businessHours;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nullable String groupId) {
        this.groupId = groupId;
    }

    public List<BusinessHours> getBusinessHours() {
        return businessHours == null ? Collections.emptyList() : businessHours;
    }

    public void setBusinessHours(@Nullable List<BusinessHours> businessHours) {
        this.businessHours = businessHours;
    }
}
