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

package io.jmix.pivottableflowui.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.jmix.pivottableflowui.kit.component.serialization.AbstractSerializer;

import java.io.IOException;
import java.util.function.Function;

public class PivotTableBooleanSerializer extends AbstractSerializer<Boolean> {

    private final Function<String, String> messagesProvider;

    public PivotTableBooleanSerializer(Function<String, String> messagesProvider) {
        super(Boolean.class);

        this.messagesProvider = messagesProvider;
    }

    @Override
    public void serializeNonNullValue(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value ? messagesProvider.apply("boolean.yes") : messagesProvider.apply("boolean.no"));
    }
}
