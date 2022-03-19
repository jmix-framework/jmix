/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components;

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.data.Datasource;

import java.sql.Date;
import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.TimeField} instead
 */
@Deprecated
public interface TimeField<V> extends Field<V>, io.jmix.ui.component.TimeField<V> {

    TypeToken<TimeField<Date>> TYPE_DEFAULT = new TypeToken<TimeField<Date>>(){};

    TypeToken<TimeField<java.sql.Time>> TYPE_TIME = new TypeToken<TimeField<java.sql.Time>>(){};
    TypeToken<TimeField<LocalTime>> TYPE_LOCALTIME = new TypeToken<TimeField<LocalTime>>(){};
    TypeToken<TimeField<OffsetTime>> TYPE_OFFSETTIME = new TypeToken<TimeField<OffsetTime>>(){};

    /**
     * @return whether the TimeField should display seconds
     *
     * @deprecated Use either {@link #getResolution()} or {@link #getFormat()}
     */
    @Deprecated
    boolean getShowSeconds();

    /**
     * Sets whether the TimeField should display seconds.
     *
     * @deprecated Use either {@link #setResolution(Resolution)} or {@link #setFormat(String)}
     */
    @Deprecated
    void setShowSeconds(boolean showSeconds);
}
