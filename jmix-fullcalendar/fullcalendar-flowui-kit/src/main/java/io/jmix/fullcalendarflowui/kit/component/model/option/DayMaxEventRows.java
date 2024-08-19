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

import jakarta.annotation.Nullable;

public class DayMaxEventRows extends CalendarOption {
    public static final String NAME = "dayMaxEventRows";

    protected boolean limited = false;
    protected Integer max;

    public DayMaxEventRows() {
        super(NAME);
    }

    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;

        markAsDirty();
    }

    @Nullable
    public Integer getMax() {
        return max;
    }

    public void setMax(@Nullable Integer max) {
        this.max = max;

        markAsDirty();
    }
}
