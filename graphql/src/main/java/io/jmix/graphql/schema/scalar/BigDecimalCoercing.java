package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.math.BigDecimal;

public class BigDecimalCoercing extends BaseScalarCoercing {

    @Override
    public Object serialize(Object input) throws CoercingSerializeException {
        return (input instanceof BigDecimal) ? String.valueOf(input) : null;
    }

    @Override
    public Object parseValue(Object input) throws CoercingParseValueException {
        return (input instanceof String) ? new BigDecimal((String) input) : null;
    }
}
