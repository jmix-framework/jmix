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
package io.jmix.ui.component;

import io.jmix.ui.component.data.HasValueSource;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetTime;

/**
 * Label component for showing non-editable short texts.
 *
 * @param <V> type of value
 */
public interface Label<V> extends Component, HasValue<V>, HasValueSource<V>, HasFormatter<V>,
        Component.HasDescription, Component.HasIcon, HasContextHelp, HasHtmlDescription, HasHtmlSanitizer {

    String NAME = "label";

    static <T, V> ParameterizedTypeReference<Label<T>> of(Class<V> valueClass) {
        return new ParameterizedTypeReference<Label<T>>() {};
    }

    ParameterizedTypeReference<Label<String>> TYPE_DEFAULT =
            new ParameterizedTypeReference<Label<String>>() {};
    ParameterizedTypeReference<Label<String>> TYPE_STRING =
            new ParameterizedTypeReference<Label<String>>() {};

    ParameterizedTypeReference<Label<Integer>> TYPE_INTEGER =
            new ParameterizedTypeReference<Label<Integer>>() {};
    ParameterizedTypeReference<Label<Long>> TYPE_LONG =
            new ParameterizedTypeReference<Label<Long>>() {};
    ParameterizedTypeReference<Label<Double>> TYPE_DOUBLE =
            new ParameterizedTypeReference<Label<Double>>() {};
    ParameterizedTypeReference<Label<BigDecimal>> TYPE_BIGDECIMAL =
            new ParameterizedTypeReference<Label<BigDecimal>>() {};

    ParameterizedTypeReference<Label<java.sql.Date>> TYPE_DATE =
            new ParameterizedTypeReference<Label<java.sql.Date>>() {};
    ParameterizedTypeReference<Label<java.util.Date>> TYPE_DATETIME =
            new ParameterizedTypeReference<Label<java.util.Date>>() {};
    ParameterizedTypeReference<Label<LocalDate>> TYPE_LOCALDATE =
            new ParameterizedTypeReference<Label<LocalDate>>() {};
    ParameterizedTypeReference<Label<LocalDateTime>> TYPE_LOCALDATETIME =
            new ParameterizedTypeReference<Label<LocalDateTime>>() {};
    ParameterizedTypeReference<Label<java.sql.Time>> TYPE_TIME =
            new ParameterizedTypeReference<Label<java.sql.Time>>() {};
    ParameterizedTypeReference<Label<OffsetTime>> TYPE_OFFSETTIME =
            new ParameterizedTypeReference<Label<OffsetTime>>() {};

    boolean isHtmlEnabled();
    void setHtmlEnabled(boolean htmlEnabled);

    /**
     * Returns a string representation of the value.
     */
    String getRawValue();
}