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

import jakarta.annotation.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.List;

public abstract class AbstractSerializer<T> extends StdSerializer<T> {

    public AbstractSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(@Nullable T value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        serializeNonNullValue(value, gen, provider);
    }

    public abstract void serializeNonNullValue(T value, JsonGenerator gen, SerializationContext provider)
            throws JacksonException;

    protected void writeIfNotNull(String fieldName, Object value, JsonGenerator gen, SerializationContext provider)
            throws JacksonException {
        if (value != null) {
            provider.defaultSerializeProperty(fieldName, value, gen);
        }
    }

    protected void writeListIfNotEmpty(String fieldName, List<?> value,
                                       JsonGenerator gen, SerializationContext provider) throws JacksonException {
        if (value != null && !value.isEmpty()) {
            provider.defaultSerializeProperty(fieldName, value, gen);
        }
    }
}

