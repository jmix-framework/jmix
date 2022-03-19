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
import io.jmix.core.metamodel.datatype.Datatype;

import java.text.ParseException;
import java.time.format.DateTimeParseException;

public class DatatypeCoercing<I> implements Coercing<I, String> {

    protected Class<I> type;
    protected Datatype<I> datatype;
    protected String incorrectFormatMessage;

    public DatatypeCoercing(Class<I> type, Datatype<I> datatype, String incorrectFormatMessage) {
        this.type = type;
        this.datatype = datatype;
        this.incorrectFormatMessage = incorrectFormatMessage;
    }

    @Override
    public String serialize(Object o) throws CoercingSerializeException {
        if (!o.getClass().isAssignableFrom(type)) {
            throw new CoercingSerializeException(
                    "Expected type '" + type.getSimpleName() + "' but was '" + o.getClass().getSimpleName() + "'.");
        }
        return datatype.format(o);
    }

    @Override
    public I parseValue(Object input) {
        if (input instanceof String) {
            String value = (String) input;
            try {
                return datatype.parse(value);
            } catch (DateTimeParseException | ParseException exception) {
                throw new CoercingParseLiteralException(incorrectFormatMessage);
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

}
