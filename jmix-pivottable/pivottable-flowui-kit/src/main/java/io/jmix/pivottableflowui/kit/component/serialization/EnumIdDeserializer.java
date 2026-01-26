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

package io.jmix.pivottableflowui.kit.component.serialization;

import io.jmix.pivottableflowui.kit.component.model.SerializedEnum;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.EnumSet;

public class EnumIdDeserializer<E extends Enum<E>> extends StdDeserializer<E> {

    private final Class<E> serializedEnumClass;

    public EnumIdDeserializer(Class<E> serializedEnumClass) {
        super(serializedEnumClass);
        this.serializedEnumClass = serializedEnumClass;
    }

    @Override
    public E deserialize(JsonParser jsonParser,
                         DeserializationContext deserializationContext) throws JacksonException {
        JsonToken token = jsonParser.currentToken();
        if (token == JsonToken.VALUE_STRING) {
            String textId = jsonParser.getString();

            return EnumSet.allOf(serializedEnumClass)
                    .stream()
                    .filter(value -> value instanceof SerializedEnum serializedEnum &&
                            serializedEnum.getId().equals(textId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}
