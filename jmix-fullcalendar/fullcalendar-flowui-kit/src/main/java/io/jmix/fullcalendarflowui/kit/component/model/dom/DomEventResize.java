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

package io.jmix.fullcalendarflowui.kit.component.model.dom;

import jakarta.annotation.Nullable;

/**
 * INTERNAL.
 */
public class DomEventResize extends AbstractEventMoveEvent {

    protected DomCalendarDuration startDelta;

    protected DomCalendarDuration endDelta;

    @Nullable
    public DomCalendarDuration getStartDelta() {
        return startDelta;
    }

    public void setStartDelta(@Nullable DomCalendarDuration startDelta) {
        this.startDelta = startDelta;
    }

    @Nullable
    public DomCalendarDuration getEndDelta() {
        return endDelta;
    }

    public void setEndDelta(@Nullable DomCalendarDuration endDelta) {
        this.endDelta = endDelta;
    }
}
