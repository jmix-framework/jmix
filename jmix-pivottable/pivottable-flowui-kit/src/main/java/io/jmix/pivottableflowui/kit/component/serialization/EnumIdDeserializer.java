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

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.jmix.pivottableflowui.kit.component.model.SerializedEnum;

import java.io.IOException;
import java.util.EnumSet;

public class EnumIdDeserializer<E extends Enum> extends JsonDeserializer<E> {
    private final Class<E> serializedEnumClass;

    public EnumIdDeserializer(Class<E> serializedEnumClass) {
        this.serializedEnumClass = serializedEnumClass;
    }

    @Override
    public E deserialize(JsonParser jsonParser,
                         DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonToken token = jsonParser.getCurrentToken();
        if (token == JsonToken.VALUE_STRING) {
            String textId = jsonParser.getText();

            return (E) EnumSet.allOf(serializedEnumClass)
                    .stream()
                    .filter(value -> value instanceof SerializedEnum serializedEnum && serializedEnum.getId().equals(textId))
                    .findFirst().orElse(null);
        }
        return null;
    }
}
