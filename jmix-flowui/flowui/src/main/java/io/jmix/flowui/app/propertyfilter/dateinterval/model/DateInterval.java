/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.app.propertyfilter.dateinterval.model;

import org.springframework.lang.Nullable;

/**
 * Class describes date interval with the following types.
 * <ul>
 *     <li>{@link Type#LAST}</li>
 *     <li>{@link Type#NEXT}</li>
 * </ul>
 */
public class DateInterval implements BaseDateInterval {

    public enum TimeUnit {
        YEAR, MONTH, DAY, HOUR, MINUTE
    }

    protected final Type type;
    protected final Integer number;
    protected final TimeUnit timeUnit;
    protected final Boolean includingCurrent;

    public DateInterval(Type type, Integer number, TimeUnit timeUnit, @Nullable Boolean includingCurrent) {
        this.type = type;
        this.number = number;
        this.timeUnit = timeUnit;
        this.includingCurrent = includingCurrent;
    }

    @Override
    public Type getType() {
        return type;
    }

    public Integer getNumber() {
        return number;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    @Nullable
    public Boolean getIncludingCurrent() {
        return includingCurrent;
    }

    @Override
    public String apply(String property) {
        String moment1 = "";
        String moment2 = "";

        if (type == Type.LAST) {
            moment1 = "now - " + number;
            moment2 = Boolean.TRUE.equals(includingCurrent) ? "now + 1" : "now";
        } else if (type == Type.NEXT) {
            moment1 = Boolean.TRUE.equals(includingCurrent) ? "now" : "now + 1";
            moment2 = "now + " + (number + 1);
        }
        return String.format("@between({E}.%s, %s, %s, %s)", property, moment1, moment2, timeUnit.name());
    }
}
