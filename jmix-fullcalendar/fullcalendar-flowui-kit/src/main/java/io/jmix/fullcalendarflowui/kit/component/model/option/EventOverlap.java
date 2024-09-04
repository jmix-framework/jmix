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

import io.jmix.fullcalendarflowui.kit.component.model.JsFunction;
import jakarta.annotation.Nullable;

import static io.jmix.fullcalendarflowui.kit.component.model.option.OptionConstants.EVENT_OVERLAP;

/**
 * INTERNAL.
 */
public class EventOverlap extends CalendarOption {

    protected boolean enabled = true;

    protected JsFunction jsFunction;

    public EventOverlap() {
        super(EVENT_OVERLAP);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        markAsDirty();
    }

    @Nullable
    public JsFunction getJsFunction() {
        return jsFunction;
    }

    public void setJsFunction(@Nullable JsFunction jsFunction) {
        this.jsFunction = jsFunction;

        markAsDirty();
    }
}
