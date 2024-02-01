/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.List;

public abstract class AbstractSerializer<T> extends StdSerializer<T> {

    public AbstractSerializer(Class<T> t) {
        super(t);
    }

    @Override
    public void serialize(@Nullable T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }

        serializeNonNullValue(value, gen, provider);
    }

    public abstract void serializeNonNullValue(T value, JsonGenerator gen, SerializerProvider provider)
            throws IOException;

    protected void writeIfNotNull(String fieldName, Object value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        if (value != null) {
            provider.defaultSerializeField(fieldName, value, gen);
        }
    }

    protected void writeListIfNotEmpty(String fieldName, List<?> value,
                                       JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null && !value.isEmpty()) {
            provider.defaultSerializeField(fieldName, value, gen);
        }
    }
}
