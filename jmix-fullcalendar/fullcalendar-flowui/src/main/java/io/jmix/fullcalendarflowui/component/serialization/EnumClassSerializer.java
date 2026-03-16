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

import io.jmix.core.metamodel.datatype.EnumClass;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class EnumClassSerializer extends StdSerializer<EnumClass> {

    public EnumClassSerializer() {
        super(EnumClass.class);
    }

    @Override
    public void serialize(EnumClass value, JsonGenerator gen, SerializationContext provider) {
        if (value.getId() instanceof String id) {
            gen.writeString(id);
        } else if (value.getId() instanceof Integer id) {
            gen.writeNumber(id);
        }
    }
}
