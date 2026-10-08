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

package io.jmix.aitools.dataload.json;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Jackson deserializer for a {@code List} field that tolerates LLM output emitting an empty JSON
 * object ({@code {}}) where a JSON array is expected, mapping it to an empty list.
 * <p>
 * Small local models non-deterministically render an empty list (such as a parameterless query's
 * parameters) as {@code {}} instead of {@code []}. Because the tolerance is attached to the target
 * field via {@link tools.jackson.databind.annotation.JsonDeserialize @JsonDeserialize}, it applies
 * regardless of which {@code ObjectMapper} performs the binding — including the mapper Spring AI
 * uses internally to bind tool-call arguments, which cannot be configured externally.
 * <p>
 * Only an <em>empty</em> object is coerced; a non-empty object where an array is expected is left to
 * be reported as a genuine mismatch. Non-object values are delegated to the standard list
 * deserialization, so populated arrays are unaffected.
 */
@NullMarked
public class EmptyObjectTolerantListDeserializer extends ValueDeserializer<List<?>> {

    @Nullable
    protected final JavaType listType;

    public EmptyObjectTolerantListDeserializer() {
        this.listType = null;
    }

    protected EmptyObjectTolerantListDeserializer(@Nullable JavaType listType) {
        this.listType = listType;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        return new EmptyObjectTolerantListDeserializer(property != null ? property.getType() : null);
    }

    @Override
    public List<?> deserialize(JsonParser p, DeserializationContext ctxt) {
        if (p.currentToken() == JsonToken.START_OBJECT) {
            if (p.nextToken() == JsonToken.END_OBJECT) {
                return new ArrayList<>();
            }
            // A non-empty object where an array is expected is a genuine malformation: report it.
            ctxt.handleUnexpectedToken(resolveListType(ctxt), JsonToken.START_OBJECT, p,
                    "Cannot deserialize a List from a non-empty JSON object");
        }
        ValueDeserializer<Object> delegate = ctxt.findRootValueDeserializer(resolveListType(ctxt));
        return (List<?>) delegate.deserialize(p, ctxt);
    }

    protected JavaType resolveListType(DeserializationContext ctxt) {
        return listType != null ? listType : ctxt.constructType(List.class);
    }
}
