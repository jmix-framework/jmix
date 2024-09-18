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

package io.jmix.fullcalendarflowui.converter;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.jmix.fullcalendarflowui.component.model.DayOfWeek;
import io.jmix.fullcalendarflowui.component.model.DaysOfWeek;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class DaysOfWeekConverter implements AttributeConverter<DaysOfWeek, String> {

    @Nullable
    @Override
    public String convertToDatabaseColumn(@Nullable DaysOfWeek daysOfWeek) {
        if (daysOfWeek == null) {
            return null;
        }

        return Joiner.on(",")
                .appendTo(new StringBuilder(),
                        daysOfWeek.getDaysOfWeek().stream()
                                .map(d -> d.getId().toString())
                                .toList())
                .toString();
    }

    @Nullable
    @Override
    public DaysOfWeek convertToEntityAttribute(@Nullable String s) {
        if (s == null) {
            return null;
        }

        List<String> dayOfWeekIds = Splitter.on(",")
                .omitEmptyStrings()
                .trimResults()
                .splitToList(s);

        return new DaysOfWeek(
                dayOfWeekIds.stream()
                        .map(d -> DayOfWeek.fromId(Integer.parseInt(d)))
                        .collect(Collectors.toSet()));
    }
}
