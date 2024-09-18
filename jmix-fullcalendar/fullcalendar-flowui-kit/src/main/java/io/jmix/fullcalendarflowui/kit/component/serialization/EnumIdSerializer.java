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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.jmix.fullcalendarflowui.kit.component.model.HasEnumId;

import java.io.IOException;

/**
 * INTERNAL.
 */
public class EnumIdSerializer extends StdSerializer<HasEnumId> {

    public EnumIdSerializer() {
        super(HasEnumId.class);
    }

    @Override
    public void serialize(HasEnumId value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        if (value.getId() instanceof String id) {
            gen.writeString(id);
        } else if (value.getId() instanceof Integer id) {
            gen.writeNumber(id);
        }
    }
}
