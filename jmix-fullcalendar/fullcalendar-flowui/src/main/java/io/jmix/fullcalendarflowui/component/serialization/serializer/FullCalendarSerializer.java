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

package io.jmix.fullcalendarflowui.component.serialization.serializer;

import com.fasterxml.jackson.databind.JsonSerializer;
import io.jmix.fullcalendarflowui.kit.component.serialization.serializer.JmixFullCalendarSerializer;

import java.util.List;

public class FullCalendarSerializer extends JmixFullCalendarSerializer {

    @Override
    protected List<JsonSerializer<?>> getSerializers() {
        List<JsonSerializer<?>> serializers = super.getSerializers();
        serializers.add(new EnumClassSerializer()); // todo affects data serialization
        serializers.add(new EventConstraintSerializer());
        serializers.add(new BusinessHoursOptionSerializer());
        serializers.add(new SelectConstraintSerializer());
        return serializers;
    }
}
