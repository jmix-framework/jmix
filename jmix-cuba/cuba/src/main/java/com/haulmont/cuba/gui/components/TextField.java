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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.util.function.Function;

/**
 * Component compatible with {@link Datasource}.
 *
 * @param <V> type of value
 * @deprecated Use {@link io.jmix.ui.component.TextField} instead
 */
@Deprecated
public interface TextField<V> extends TextInputField<V>, io.jmix.ui.component.TextField<V>,
        TextInputField.TextChangeNotifier, TextInputField.EnterPressNotifier {

    TypeToken<TextField<String>> TYPE_DEFAULT = new TypeToken<TextField<String>>(){};
    TypeToken<TextField<String>> TYPE_STRING = new TypeToken<TextField<String>>(){};

    TypeToken<TextField<Integer>> TYPE_INTEGER = new TypeToken<TextField<Integer>>(){};
    TypeToken<TextField<Long>> TYPE_LONG = new TypeToken<TextField<Long>>(){};
    TypeToken<TextField<Double>> TYPE_DOUBLE = new TypeToken<TextField<Double>>(){};
    TypeToken<TextField<BigDecimal>> TYPE_BIGDECIMAL = new TypeToken<TextField<BigDecimal>>(){};

    TypeToken<TextField<java.sql.Date>> TYPE_DATE = new TypeToken<TextField<java.sql.Date>>(){};
    TypeToken<TextField<java.util.Date>> TYPE_DATETIME = new TypeToken<TextField<java.util.Date>>(){};
    TypeToken<TextField<LocalDate>> TYPE_LOCALDATE = new TypeToken<TextField<LocalDate>>(){};
    TypeToken<TextField<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<TextField<LocalDateTime>>(){};
    TypeToken<TextField<java.sql.Time>> TYPE_TIME = new TypeToken<TextField<java.sql.Time>>(){};
    TypeToken<TextField<OffsetTime>> TYPE_OFFSETTIME = new TypeToken<TextField<OffsetTime>>(){};

    @Deprecated
    default void setFormatter(Function<? super V, String> formatter) {
        setFormatter(formatter::apply);
    }
}
