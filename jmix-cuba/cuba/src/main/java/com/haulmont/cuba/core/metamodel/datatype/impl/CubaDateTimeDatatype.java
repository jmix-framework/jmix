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

package com.haulmont.cuba.core.metamodel.datatype.impl;

import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.DateTimeFormat;
import io.jmix.core.metamodel.datatype.impl.DateTimeDatatype;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@DatatypeDef(id = "dateTime", javaClass = Date.class, defaultForClass = true, value = "cuba_DateTimeDatatype")
@DateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS")
public class CubaDateTimeDatatype extends DateTimeDatatype {

    private String formatPattern;

    public CubaDateTimeDatatype() {
        DateTimeFormat dateTimeFormat = getClass().getAnnotation(DateTimeFormat.class);
        if (dateTimeFormat != null) {
            formatPattern = dateTimeFormat.value();
        }
    }

    @Override
    public String format(@Nullable Object value) {
        if (value == null) {
            return "";
        } else {
            DateFormat format;
            if (formatPattern != null) {
                format = new SimpleDateFormat(formatPattern);
            } else {
                format = DateFormat.getDateInstance();
            }
            return format.format((value));
        }
    }

    @Override
    public Date parse(@Nullable String value) throws ParseException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        DateFormat format;
        if (formatPattern != null) {
            format = new SimpleDateFormat(formatPattern);
        } else {
            format = DateFormat.getDateInstance();
        }
        return format.parse(value.trim());
    }

    @Override
    public Map<String, Object> getParameters() {
        return ParamsMap.of("format", formatPattern);
    }


}
