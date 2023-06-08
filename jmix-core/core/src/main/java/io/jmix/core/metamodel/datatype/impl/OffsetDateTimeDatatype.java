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

package io.jmix.core.metamodel.datatype.impl;

import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.TimeZoneAwareDatatype;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalQuery;
import java.util.Locale;
import java.util.TimeZone;

@Internal
@DatatypeDef(id = "offsetDateTime", javaClass = OffsetDateTime.class, defaultForClass = true, value = "core_OffsetDateTimeDatatype")
public class OffsetDateTimeDatatype extends AbstractTemporalDatatype<OffsetDateTime>
        implements TimeZoneAwareDatatype {

    public OffsetDateTimeDatatype() {
        super(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Override
    public String format(@Nullable Object value, Locale locale, @Nullable TimeZone timeZone) {
        if (timeZone == null || value == null) {
            return format(value, locale);
        }
        OffsetDateTime offsetDateTime = (OffsetDateTime) value;
        ZonedDateTime zonedDateTime = offsetDateTime.atZoneSameInstant(timeZone.toZoneId());
        return format(zonedDateTime, locale);
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter(FormatStrings formatStrings, Locale locale) {
        return DateTimeFormatter.ofPattern(formatStrings.getOffsetDateTimeFormat(), locale);
    }

    @Override
    protected TemporalQuery<OffsetDateTime> newInstance() {
        return OffsetDateTime::from;
    }
}