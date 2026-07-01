/*
 * Copyright 2026 Haulmont.
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

package io.jmix.fullcalendarflowui.component;

import io.jmix.core.annotation.Internal;
import io.jmix.fullcalendarflowui.component.data.AbstractDataProviderManager;
import io.jmix.fullcalendarflowui.kit.component.model.CalendarDisplayMode;
import org.jspecify.annotations.Nullable;

/**
 * INTERNAL.
 */
@Internal
public final class FullCalendarUtils {

    private FullCalendarUtils() {
    }

    @Nullable
    public static AbstractDataProviderManager getDataProviderManager(FullCalendar fullCalendar, String sourceId) {
        return fullCalendar.dataProvidersMap.values().stream()
                .filter(em -> em.getSourceId().equals(sourceId))
                .findFirst()
                .orElse(null);
    }

    public static CalendarDisplayMode getDisplayMode(FullCalendar fullCalendar, String displayModeId) {
        return fullCalendar.getDisplayMode(displayModeId);
    }
}
