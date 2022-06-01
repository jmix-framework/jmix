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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * A date entry component, which displays the actual date selector or date with time.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "DateField",
        category = "Components",
        xmlElement = "dateField",
        icon = "io/jmix/ui/icon/component/dateField.svg",
        canvasBehaviour = CanvasBehaviour.DATE_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/date-field.html"
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
public interface DateField<V> extends Field<V>, HasDatatype<V>, Buffered, Component.Focusable, HasRange<V> {
    String NAME = "dateField";

    ParameterizedTypeReference<DateField<java.util.Date>> TYPE_DEFAULT =
            new ParameterizedTypeReference<DateField<java.util.Date>>() {};

    ParameterizedTypeReference<DateField<java.sql.Date>> TYPE_DATE =
            new ParameterizedTypeReference<DateField<java.sql.Date>>() {};
    ParameterizedTypeReference<DateField<java.util.Date>> TYPE_DATETIME =
            new ParameterizedTypeReference<DateField<java.util.Date>>() {};
    ParameterizedTypeReference<DateField<LocalDate>> TYPE_LOCALDATE =
            new ParameterizedTypeReference<DateField<LocalDate>>() {};
    ParameterizedTypeReference<DateField<LocalDateTime>> TYPE_LOCALDATETIME =
            new ParameterizedTypeReference<DateField<LocalDateTime>>() {};
    ParameterizedTypeReference<DateField<OffsetDateTime>> TYPE_OFFSETDATETIME =
            new ParameterizedTypeReference<DateField<OffsetDateTime>>() {};

    enum Resolution {
        SEC,
        MIN,
        HOUR,
        DAY,
        MONTH,
        YEAR
    }

    @Nullable
    Resolution getResolution();

    @StudioProperty(type = PropertyType.ENUMERATION, options = {"YEAR", "MONTH", "DAY", "HOUR", "MIN", "SEC"})
    void setResolution(Resolution resolution);

    @Nullable
    String getDateFormat();

    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setDateFormat(String dateFormat);

    /**
     * Use {@link DateField#getZoneId()}
     */
    @Nullable
    TimeZone getTimeZone();

    /**
     * Use {@link DateField#setZoneId(ZoneId)}
     */
    void setTimeZone(@Nullable TimeZone timeZone);

    void setZoneId(@Nullable ZoneId zoneId);

    @Nullable
    ZoneId getZoneId();

    /**
     * Sets whether autofill feature is enabled.
     * <p>
     * When enabled uses current month and year.
     *
     * @param autofill whether autofill is enabled
     */
    @StudioProperty(defaultValue = "false")
    void setAutofill(boolean autofill);

    /**
     * @return whether autofill is enabled
     */
    boolean isAutofill();

    /**
     * Sets time mode to use (12h AM/PM or 24h).
     * <p>
     * By default the 24h mode is used.
     *
     * @param timeMode time mode
     */
    @StudioProperty(name = "timeMode", type = PropertyType.ENUMERATION, defaultValue = "H_24", options = {"H_12", "H_24"})
    void setTimeMode(TimeField.TimeMode timeMode);

    /**
     * @return {@link TimeField.TimeMode} that is used by component
     */
    @Nullable
    TimeField.TimeMode getTimeMode();
}
