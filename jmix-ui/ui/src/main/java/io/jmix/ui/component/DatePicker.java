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

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;
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
@StudioComponent(
        caption = "DatePicker",
        category = "Components",
        xmlElement = "datePicker",
        icon = "io/jmix/ui/icon/component/datePicker.svg",
        canvasBehaviour = CanvasBehaviour.DATE_PICKER,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/date-picker.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF,
                        options = {"date", "dateTime", "localDate", "localDateTime", "offsetDateTime"},
                        typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "datatype", type = PropertyType.DATATYPE_ID, options = {"date", "dateTime",
                        "localDate", "localDateTime", "offsetDateTime"}, typeParameter = "V")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
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
     * Returns resolution of the DatePicker.
     *
     * @return Resolution
     */
    Resolution getResolution();

    /**
     * Sets resolution of the DatePicker.
     *
     * @param resolution resolution
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "DAY", options = {"YEAR", "MONTH", "DAY"})
    void setResolution(Resolution resolution);

    @Nullable
    @Override
    V getValue();
}