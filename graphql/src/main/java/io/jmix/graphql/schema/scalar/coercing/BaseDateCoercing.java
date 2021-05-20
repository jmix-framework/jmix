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

import java.time.format.DateTimeParseException;

public abstract class BaseDateCoercing implements Coercing {

    private final String format;

    public BaseDateCoercing(String format) {
        this.format = format;
    }

    @Override
    public Object parseValue(Object input) {
        if (input instanceof String) {
            String value = (String) input;
            try {
                return parseString(value);
            } catch (DateTimeParseException exception) {
                throw new CoercingParseLiteralException("Please use the format " + format);
            }
        }
        throw new CoercingParseLiteralException(
                "Expected type 'String' but was '" + input.getClass().getSimpleName() + "'.");
    }

    @Override
    public Object parseLiteral(Object input) {
        if (input instanceof StringValue) {
            String value = ((StringValue) input).getValue();
            try {
                return parseString(value);
            } catch (Exception exception) {
                throw new CoercingParseLiteralException("Please use the format " + format);
            }
        }
        throw new CoercingParseLiteralException(
                "Expected type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");

    }

    protected abstract Object parseString(String value);

}
