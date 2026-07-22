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

package io.jmix.aitools.dataload.repair.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Jackson problem handler that tolerates LLM output emitting an empty JSON object ({@code {}})
 * where a JSON array is expected, mapping it to an empty collection.
 */
@NullMarked
public class EmptyObjectAsEmptyCollectionHandler extends DeserializationProblemHandler {

    /**
     * Coerces an empty JSON object into an empty collection when a collection value was expected,
     * otherwise defers to the default (unhandled) behavior.
     *
     * @param ctxt       active deserialization context
     * @param targetType the type Jackson tried to bind the value to
     * @param t          the unexpected token encountered (the current token of {@code p})
     * @param p          the JSON parser positioned at the unexpected token
     * @param failureMsg the default failure message
     * @return an empty {@link ArrayList} when an empty object was found in place of an array,
     * otherwise the result of the default handling (which reports the mismatch)
     * @throws IOException if reading from the parser fails
     */
    @Override
    public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType,
                                        JsonToken t, JsonParser p, String failureMsg) throws IOException {
        if (t == JsonToken.START_OBJECT
                && targetType.isCollectionLikeType()
                && p.nextToken() == JsonToken.END_OBJECT) {
            return new ArrayList<>();
        }
        return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg);
    }
}
