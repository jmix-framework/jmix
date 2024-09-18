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

package io.jmix.fullcalendarflowui.datatype;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.Ddl;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.DaysOfWeek;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * The datatype that enables containing days of week in one field.
 */
@DatatypeDef(id = "daysOfWeek", javaClass = DaysOfWeek.class, defaultForClass = true, value = "fcalen_DaysOfWeek")
@Ddl("varchar(200)")
public class DaysOfWeekDatatype implements Datatype<DaysOfWeek> {

    protected Messages messages;

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public String format(@Nullable Object value) {
        return format(value, Locale.ENGLISH);
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        if (value instanceof DaysOfWeek daysOfWeek) {
            List<DayOfWeek> days = sortDaysOfWeek(daysOfWeek.getDaysOfWeek(), locale);

            return Joiner.on(", ")
                    .appendTo(new StringBuilder(),
                            days.stream().map((d) -> messages.getMessage(d, locale)).toList())
                    .toString();
        }
        return "";
    }

    @Override
    public DaysOfWeek parse(@Nullable String value) throws ParseException {
        return parse(value, Locale.ENGLISH);
    }

    @Override
    public DaysOfWeek parse(@Nullable String value, Locale locale) throws ParseException {
        if (value == null) {
            return null;
        }
        if (value.trim().isEmpty()) {
            return new DaysOfWeek(Set.of());
        }
        List<String> localizedDaysOfWeek = Splitter.on(", ")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(value);

        List<DayOfWeek> allDaysOfWeek = Arrays.stream(DayOfWeek.values()).toList();
        Set<DayOfWeek> resultDaysOfWeek = new HashSet<>(localizedDaysOfWeek.size());

        for (String localizedDay : localizedDaysOfWeek) {
            allDaysOfWeek.stream()
                    .filter(d -> messages.getMessage(d, locale).equals(localizedDay))
                    .findFirst()
                    .ifPresent(resultDaysOfWeek::add);
        }
        return new DaysOfWeek(resultDaysOfWeek);
    }

    protected List<DayOfWeek> sortDaysOfWeek(Collection<DayOfWeek> value, Locale locale) {
        DayOfWeek firstDay = DayOfWeek.fromDayOfWeek(WeekFields.of(locale).getFirstDayOfWeek());

        return DaysOfWeekDatatypeUtils.sortByFirstDay(value, firstDay);
    }
}
