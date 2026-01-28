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

package io.jmix.fullcalendarflowui.kit.component.serialization;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.module.SimpleModule;

import java.util.ArrayList;
import java.util.List;

/**
 * INTERNAL.
 */
public abstract class AbstractFullCalendarSerializer {

    protected ObjectMapper objectMapper;

    public AbstractFullCalendarSerializer() {
        objectMapper = createObjectMapper();
        objectMapper = initObjectMapper(objectMapper);
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }

    protected ObjectMapper initObjectMapper(ObjectMapper objectMapper) {
        SimpleModule module = new SimpleModule();
        getSerializers().forEach(module::addSerializer);

        return objectMapper.rebuild()
                .addModule(module)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();
    }

    protected List<ValueSerializer<?>> getSerializers() {
        List<ValueSerializer<?>> serializers = new ArrayList<>(11);
        serializers.add(new EnumIdSerializer());
        serializers.add(new JsFunctionSerializer());
        serializers.add(new LocalDateIsoSerializer());
        serializers.add(new LocalTimeIsoSerializer());
        serializers.add(new LocalDateTimeIsoSerializer());
        serializers.add(new ZonedDateTimeIsoSerializer());
        serializers.add(new TimeZoneSerializer());
        return serializers;
    }
}
