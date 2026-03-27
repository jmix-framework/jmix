/*
 * Copyright 2026 Haulmont.
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

package io.jmix.reports.libintegration;

import io.jmix.core.Entity;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;

/**
 * The {@code ReportValueConverter} class is responsible for converting report-related values into a format
 * that can be processed further.
 */
@Component("report_ReportValueConverter")
public class ReportValueConverter {

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Nullable
    public Object convertValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }

        if (datatypeRegistry.find(value.getClass()) != null
                || value instanceof Entity
                || value instanceof EnumClass) {
            return value;
        }

        String className = value.getClass().getName();
        if ("oracle.sql.TIMESTAMP".equals(className)) {
            Object convertedValue = tryInvokeMethod(value, "timestampValue");
            if (convertedValue != null) {
                return convertedValue;
            }
        }

        if ("oracle.sql.TIMESTAMPTZ".equals(className) || "oracle.sql.TIMESTAMPLTZ".equals(className)) {
            Object convertedValue = tryInvokeMethod(value, "zonedDateTimeValue");
            if (convertedValue == null) {
                convertedValue = tryInvokeMethod(value, "toZonedDateTime");
            }

            if (convertedValue instanceof ZonedDateTime zonedDateTime) {
                return zonedDateTime.toOffsetDateTime();
            }

            convertedValue = tryInvokeMethod(value, "offsetDateTimeValue");
            if (convertedValue == null) {
                convertedValue = tryInvokeMethod(value, "toOffsetDateTime");
            }

            if (convertedValue instanceof OffsetDateTime) {
                return convertedValue;
            }

            convertedValue = tryInvokeMethod(value, "toOffsetTime");
            if (convertedValue instanceof OffsetTime) {
                return convertedValue;
            }
        }

        if ("oracle.sql.DATE".equals(className)) {
            Object convertedValue = tryInvokeMethod(value, "dateValue");
            if (convertedValue != null) {
                return convertedValue;
            }
        }

        return value;
    }

    @Nullable
    protected Object tryInvokeMethod(Object value, String methodName) {
        try {
            return value.getClass().getMethod(methodName).invoke(value);
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }
}
