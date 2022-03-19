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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.TextArea} instead
 */
@Deprecated
public interface TextArea<V> extends TextInputField<V>, io.jmix.ui.component.TextArea<V>,
        TextInputField.TextChangeNotifier {

    TypeToken<TextArea<String>> TYPE_DEFAULT = new TypeToken<TextArea<String>>(){};
    TypeToken<TextArea<String>> TYPE_STRING = new TypeToken<TextArea<String>>(){};

    TypeToken<TextArea<Integer>> TYPE_INTEGER = new TypeToken<TextArea<Integer>>(){};
    TypeToken<TextArea<Long>> TYPE_LONG = new TypeToken<TextArea<Long>>(){};
    TypeToken<TextArea<Double>> TYPE_DOUBLE = new TypeToken<TextArea<Double>>(){};
    TypeToken<TextArea<BigDecimal>> TYPE_BIGDECIMAL = new TypeToken<TextArea<BigDecimal>>(){};

    TypeToken<TextArea<java.sql.Date>> TYPE_DATE = new TypeToken<TextArea<java.sql.Date>>(){};
    TypeToken<TextArea<java.util.Date>> TYPE_DATETIME = new TypeToken<TextArea<java.util.Date>>(){};
    TypeToken<TextArea<LocalDate>> TYPE_LOCALDATE = new TypeToken<TextArea<LocalDate>>(){};
    TypeToken<TextArea<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<TextArea<LocalDateTime>>(){};
    TypeToken<TextArea<java.sql.Time>> TYPE_TIME = new TypeToken<TextArea<java.sql.Time>>(){};
    TypeToken<TextArea<OffsetTime>> TYPE_OFFSETTIME = new TypeToken<TextArea<OffsetTime>>(){};

    /**
     * @return the number of columns
     * @deprecated Use {@link #getWidth()} instead
     */
    @Deprecated
    int getColumns();

    /**
     * Sets width according to the number of columns.
     *
     * @param columns the number of columns to set
     * @deprecated Use {@link #setWidth(String)} instead
     */
    @Deprecated
    void setColumns(int columns);
}
