package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class LongCoercing extends BaseScalarCoercing {

    @Override
    public Object serialize(Object input) throws CoercingSerializeException {
        return (input instanceof Long) ? String.valueOf(input) : null;
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return (input instanceof String) ? Long.parseLong((String) input) : null;
    }

}
