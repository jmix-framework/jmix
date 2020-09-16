/*
 * Copyright 2019 Haulmont.
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.DatePicker} instead
 */
@Deprecated
public interface DatePicker<V> extends Field<V>, io.jmix.ui.component.DatePicker<V> {

    TypeToken<DatePicker<Date>> TYPE_DEFAULT = new TypeToken<DatePicker<Date>>(){};

    TypeToken<DatePicker<Date>> TYPE_DATE = new TypeToken<DatePicker<java.sql.Date>>(){};
    TypeToken<DatePicker<java.util.Date>> TYPE_DATETIME = new TypeToken<DatePicker<java.util.Date>>(){};
    TypeToken<DatePicker<LocalDate>> TYPE_LOCALDATE = new TypeToken<DatePicker<LocalDate>>(){};
    TypeToken<DatePicker<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<DatePicker<LocalDateTime>>(){};
    TypeToken<DatePicker<OffsetDateTime>> TYPE_OFFSETDATETIME = new TypeToken<DatePicker<OffsetDateTime>>(){};
}
