/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.validation;

import io.jmix.core.TimeSource;
import io.jmix.flowui.component.validation.number.*;
import io.jmix.flowui.component.validation.time.AbstractTimeValidator.*;
import io.jmix.flowui.component.validation.time.TimeValidator;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;

public final class ValidatorHelper {

    @Nullable
    public static NumberConstraint getNumberConstraint(@Nullable Number value) {
        if (value == null) {
            return null;
        }

        Class<?> clazz = value.getClass();
        if (clazz.equals(Integer.class) || clazz.equals(BigInteger.class)) {
            return new BigIntegerConstraint(BigInteger.valueOf(value.longValue()));
        } else if (clazz.equals(Long.class)) {
            return new LongConstraint(value.longValue());
        } else if (clazz.equals(BigDecimal.class)) {
            return new BigDecimalConstraint((BigDecimal) value);
        } else if (clazz.equals(Double.class)) {
            return new DoubleConstraint(value.doubleValue());
        } else if (clazz.equals(Float.class)) {
            return new FloatConstraint(value.floatValue());
        }
        return null;
    }

    @Nullable
    public static <T> TimeValidator getTimeConstraint(TimeSource timeSource, @Nullable T value) {
        if (value == null) {
            return null;
        }

        Class<?> clazz = value.getClass();
        if (clazz.equals(Date.class)) {
            return new DateConstraint(timeSource, (Date) value);
        } else if (clazz.equals(LocalDate.class)) {
            return new LocalDateConstraint(timeSource, (LocalDate) value);
        } else if (clazz.equals(java.sql.Date.class)) {
            return new LocalDateConstraint(timeSource, ((java.sql.Date) value).toLocalDate());
        } else if (clazz.equals(LocalDateTime.class)) {
            return new LocalDateTimeConstraint(timeSource, (LocalDateTime) value);
        } else if (clazz.equals(LocalTime.class)) {
            return new LocalTimeConstraint(timeSource, (LocalTime) value);
        } else if (clazz.equals(OffsetTime.class)) {
            return new OffsetTimeConstraint(timeSource, (OffsetTime) value);
        } else if (clazz.equals(OffsetDateTime.class)) {
            return new OffsetDateTimeConstraint(timeSource, (OffsetDateTime) value);
        }
        return null;
    }
}
