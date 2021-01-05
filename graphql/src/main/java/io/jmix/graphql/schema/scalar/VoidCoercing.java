package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

public class VoidCoercing extends BaseScalarCoercing {
    @Override
    public Object serialize(Object dataFetcherResult) throws CoercingSerializeException {
        return null;
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return null;
    }
}
