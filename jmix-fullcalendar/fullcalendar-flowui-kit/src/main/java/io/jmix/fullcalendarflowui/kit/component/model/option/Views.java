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

import io.jmix.fullcalendarflowui.kit.component.model.CalendarCustomView;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Views extends CalendarOption {
    public static final String NAME = "views";

    protected DayGridWeekViewProperties dayGridWeek;

    protected List<CalendarCustomView> customViews;

    public Views() {
        super(NAME);

        dayGridWeek = new DayGridWeekViewProperties();
        dayGridWeek.addChangeListener(this::onOptionChange);
    }

    public DayGridWeekViewProperties getDayGridWeek() {
        return dayGridWeek;
    }

    public List<CalendarCustomView> getCustomViews() {
        return customViews == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(customViews);
    }

    public void addCustomView(CalendarCustomView customView) {
        if (customViews == null) {
            customViews = new ArrayList<>();
        }

        if (!customViews.contains(customView)) {
            customViews.add(customView);
            markAsDirty();
        }
    }

    public void removeCustomView(CalendarCustomView customView) {
        if (customViews == null) {
            return;
        }

        if (customViews.remove(customView)) {
            markAsDirty();
        }
    }

    @Nullable
    public CalendarCustomView getCustomView(String id) {
        if (customViews == null) {
            return null;
        }
        return customViews.stream()
                .filter(c -> c.getCalendarView().getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    protected void onOptionChange(OptionChangeEvent event) {
        markAsDirty();
    }
}
