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

package io.jmix.fullcalendarflowui.kit.component.model;

import jakarta.annotation.Nullable;

public abstract class AbstractListViewProperties extends AbstractCalendarViewProperties {

    protected String listDayFormat;

    protected String listDaySideFormat;

    public AbstractListViewProperties(String name) {
        super(name);
    }

    @Nullable
    public String getListDayFormat() {
        return listDayFormat;
    }

    public void setListDayFormat(@Nullable String listDayFormat) {
        this.listDayFormat = listDayFormat;

        markAsDirty();
    }

    @Nullable
    public String getListDaySideFormat() {
        return listDaySideFormat;
    }

    public void setListDaySideFormat(@Nullable String listDaySideFormat) {
        this.listDaySideFormat = listDaySideFormat;

        markAsDirty();
    }
}
