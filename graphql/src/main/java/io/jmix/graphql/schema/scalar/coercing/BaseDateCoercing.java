/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.schema.scalar.coercing;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;

import java.text.ParseException;
import java.time.format.DateTimeParseException;

public abstract class BaseDateCoercing<I, O> implements Coercing<I, O> {

    protected final String format;
    protected final Class<I> type;

    public BaseDateCoercing(String format, Class<I> type) {
        this.format = format;
        this.type = type;
    }

    @Override
    public O serialize(Object input) {
        if (input.getClass().isAssignableFrom(type)) {
            return doSerialize((I) input);
        }
        throw new CoercingSerializeException(
                "Expected type '" + type.getSimpleName() + "' but was '" + input.getClass().getSimpleName() + "'.");
    }

    @Override
    public I parseValue(Object input) {
        if (input instanceof String) {
            String value = (String) input;
            if (value.isEmpty()) {
                return null;
            }
            try {
                return parseString(value);
            } catch (DateTimeParseException | ParseException | IllegalArgumentException exception) {
                throw new CoercingParseLiteralException("Please use the format " + format);
            }
        }
        throw new CoercingParseLiteralException(
                "Expected type 'String' but was '" + input.getClass().getSimpleName() + "'.");
    }

    @Override
    public I parseLiteral(Object input) {
        if (input instanceof StringValue) {
            return parseValue(((StringValue) input).getValue());
        }
        throw new CoercingParseLiteralException(
                "Expected type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");

    }

    protected abstract O doSerialize(I input);

    protected abstract I parseString(String value) throws ParseException;

}
