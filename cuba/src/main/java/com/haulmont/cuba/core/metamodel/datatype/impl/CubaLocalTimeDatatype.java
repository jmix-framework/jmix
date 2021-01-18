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

package com.haulmont.cuba.core.metamodel.datatype.impl;

import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.core.metamodel.datatype.FormatStrings;
import io.jmix.core.metamodel.datatype.impl.AbstractTemporalDatatype;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalQuery;
import java.util.Locale;

@DatatypeDef(id = "localTime", javaClass = LocalTime.class, defaultForClass = true, value = "cuba_LocalTimeDatatype")
@DateTimeFormat("HH:mm:ss")
public class CubaLocalTimeDatatype extends AbstractTemporalDatatype<LocalTime> {

    @Override
    protected DateTimeFormatter getDateTimeFormatter() {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
    }

    @Override
    protected DateTimeFormatter getDateTimeFormatter(FormatStrings formatStrings, Locale locale) {
        return DateTimeFormatter.ofPattern(formatStrings.getTimeFormat(), locale);
    }

    @Override
    protected TemporalQuery<LocalTime> newInstance() {
        return LocalTime::from;
    }
}