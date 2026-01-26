/*
 * Copyright 2026 Haulmont.
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

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static io.jmix.pivottableflowui.kit.component.serialization.JmixPivotTableSerializer.DEFAULT_DATE_TIME_FORMAT;

public class DefaultDateSerializer extends StdSerializer<Date> {

    protected final DateFormat dateFormatter = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);

    protected DefaultDateSerializer() {
        super(Date.class);
    }

    @Override
    public void serialize(Date value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        gen.writeString(dateFormatter.format(value));
    }
}