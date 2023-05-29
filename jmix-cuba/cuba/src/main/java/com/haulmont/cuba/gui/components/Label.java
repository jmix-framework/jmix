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
 * @param <V> tpe of value
 * @deprecated Use {@link io.jmix.ui.component.Label} instead
 */
@Deprecated
public interface Label<V> extends DatasourceComponent<V>, io.jmix.ui.component.Label<V> {

    static <T, V> TypeToken<Label<T>> of(Class<V> valueClass) {
        return new TypeToken<Label<T>>(){};
    }

    TypeToken<Label<String>> TYPE_DEFAULT = new TypeToken<Label<String>>(){};
    TypeToken<Label<String>> TYPE_STRING = new TypeToken<Label<String>>(){};
    TypeToken<Label<Integer>> TYPE_INTEGER = new TypeToken<Label<Integer>>(){};
    TypeToken<Label<Long>> TYPE_LONG = new TypeToken<Label<Long>>(){};
    TypeToken<Label<Double>> TYPE_DOUBLE = new TypeToken<Label<Double>>(){};
    TypeToken<Label<BigDecimal>> TYPE_BIGDECIMAL = new TypeToken<Label<BigDecimal>>(){};

    TypeToken<Label<java.sql.Date>> TYPE_DATE = new TypeToken<Label<java.sql.Date>>(){};
    TypeToken<Label<java.util.Date>> TYPE_DATETIME = new TypeToken<Label<java.util.Date>>(){};
    TypeToken<Label<LocalDate>> TYPE_LOCALDATE = new TypeToken<Label<LocalDate>>(){};
    TypeToken<Label<LocalDateTime>> TYPE_LOCALDATETIME = new TypeToken<Label<LocalDateTime>>(){};
    TypeToken<Label<java.sql.Time>> TYPE_TIME = new TypeToken<Label<java.sql.Time>>(){};
    TypeToken<Label<OffsetTime>> TYPE_OFFSETTIME = new TypeToken<Label<OffsetTime>>(){};

    @Deprecated
    default void setFormatter(Function<? super V, String> formatter) {
        setFormatter(formatter::apply);
    }
}
