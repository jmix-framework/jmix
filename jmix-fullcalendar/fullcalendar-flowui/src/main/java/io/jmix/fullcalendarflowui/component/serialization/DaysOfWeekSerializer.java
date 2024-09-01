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

package io.jmix.fullcalendarflowui.component.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.jmix.fullcalendar.DayOfWeek;
import io.jmix.fullcalendar.DaysOfWeek;

import java.io.IOException;

public class DaysOfWeekSerializer extends StdSerializer<DaysOfWeek> {

    public DaysOfWeekSerializer() {
        super(DaysOfWeek.class);
    }

    @Override
    public void serialize(DaysOfWeek value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value.getDaysOfWeek().isEmpty()) {
            return;
        }
        int[] days = value.getDaysOfWeek().stream()
                .mapToInt(DayOfWeek::getId)
                .toArray();
        gen.writeArray(days, 0, days.length);
    }
}
