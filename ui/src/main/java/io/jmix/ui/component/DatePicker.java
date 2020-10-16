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

import org.springframework.core.ParameterizedTypeReference;

import javax.annotation.Nullable;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * A date entry component, which displays the actual date selector inline.
 *
 * @param <V> type of value
 */
public interface DatePicker<V> extends Field<V>, HasDatatype<V>, Component.Focusable, HasRange<V>, Buffered {
    String NAME = "datePicker";

    ParameterizedTypeReference<DatePicker<Date>> TYPE_DEFAULT =
            new ParameterizedTypeReference<DatePicker<Date>>() {};

    ParameterizedTypeReference<DatePicker<Date>> TYPE_DATE =
            new ParameterizedTypeReference<DatePicker<Date>>() {};
    ParameterizedTypeReference<DatePicker<java.util.Date>> TYPE_DATETIME =
            new ParameterizedTypeReference<DatePicker<java.util.Date>>() {};
    ParameterizedTypeReference<DatePicker<LocalDate>> TYPE_LOCALDATE =
            new ParameterizedTypeReference<DatePicker<LocalDate>>() {};
    ParameterizedTypeReference<DatePicker<LocalDateTime>> TYPE_LOCALDATETIME =
            new ParameterizedTypeReference<DatePicker<LocalDateTime>>() {};
    ParameterizedTypeReference<DatePicker<OffsetDateTime>> TYPE_OFFSETDATETIME =
            new ParameterizedTypeReference<DatePicker<OffsetDateTime>>() {};

    enum Resolution {
        DAY,
        MONTH,
        YEAR
    }

    /**
     * Return resolution of the DatePicker.
     *
     * @return Resolution
     */
    Resolution getResolution();
    /**
     * Set resolution of the DatePicker.
     *
     * @param resolution resolution
     */
    void setResolution(Resolution resolution);

    @Nullable
    @Override
    V getValue();
}