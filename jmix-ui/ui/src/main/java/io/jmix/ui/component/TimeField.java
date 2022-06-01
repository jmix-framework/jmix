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

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.OffsetTime;

/**
 * A time entry component, which displays the actual time.
 *
 * @param <V> type of value
 */
@StudioComponent(
        caption = "TimeField",
        category = "Components",
        xmlElement = "timeField",
        icon = "io/jmix/ui/icon/component/timeField.svg",
        canvasBehaviour = CanvasBehaviour.TIME_FIELD,
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/time-field.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF,
                        options = {"time", "localTime", "offsetTime"}, typeParameter = "V"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF),
                @StudioProperty(name = "datatype", type = PropertyType.DATATYPE_ID,
                        options = {"time", "localTime", "offsetTime"}, typeParameter = "V")
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface TimeField<V> extends Field<V>, HasDatatype<V>, Buffered, Component.Focusable {
    String NAME = "timeField";

    ParameterizedTypeReference<TimeField<Date>> TYPE_DEFAULT =
            new ParameterizedTypeReference<TimeField<Date>>() {};

    ParameterizedTypeReference<TimeField<Time>> TYPE_TIME =
            new ParameterizedTypeReference<TimeField<Time>>() {};
    ParameterizedTypeReference<TimeField<LocalTime>> TYPE_LOCALTIME =
            new ParameterizedTypeReference<TimeField<LocalTime>>() {};
    ParameterizedTypeReference<TimeField<OffsetTime>> TYPE_OFFSETTIME =
            new ParameterizedTypeReference<TimeField<OffsetTime>>() {};

    enum Resolution {
        SEC,
        MIN,
        HOUR
    }

    /**
     * Returns the resolution of the TimeField.
     *
     * @return Resolution
     */
    Resolution getResolution();

    /**
     * Sets the resolution of the TimeField.
     *
     * @param resolution resolution
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "MIN",
            options = {"HOUR", "MIN", "SEC"})
    void setResolution(Resolution resolution);

    /**
     * Returns the time format of the TimeField.
     *
     * @return time format
     */
    String getFormat();

    /**
     * Sets the time format of the TimeField. It can be either a format string, or a key in message group.
     *
     * @param timeFormat time format
     */
    @StudioProperty(name = "timeFormat", type = PropertyType.LOCALIZED_STRING)
    void setFormat(String timeFormat);

    /**
     * Sets time mode to use (12h AM/PM or 24h).
     * <p>
     * By default the 24h mode is used.
     *
     * @param timeMode time mode
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "H_24", options = {"H_12", "H_24"})
    void setTimeMode(TimeMode timeMode);

    /**
     * @return {@link TimeMode} that is used by component
     */
    TimeMode getTimeMode();

    /**
     * Defines component time mode (12h AP/PM or 24h).
     */
    enum TimeMode {
        H_12,
        H_24
    }
}
