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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.DateField} instead
 */
@Deprecated
public interface DateField<V> extends Field<V>, io.jmix.ui.component.DateField<V> {

    TypeToken<DateField<Date>> TYPE_DEFAULT = new TypeToken<DateField<java.util.Date>>(){};

    TypeToken<DateField<java.sql.Date>> TYPE_DATE = new TypeToken<DateField<java.sql.Date>>(){};
    TypeToken<DateField<java.util.Date>> TYPE_DATETIME = new TypeToken<DateField<java.util.Date>>(){};
    TypeToken<DateField<LocalDate>> TYPE_LOCALDATE = new TypeToken<DateField<LocalDate>>(){};
    TypeToken<DateField<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<DateField<LocalDateTime>>(){};
    TypeToken<DateField<OffsetDateTime>> TYPE_OFFSETDATETIME = new TypeToken<DateField<OffsetDateTime>>(){};
}
