package io.jmix.graphql.schema.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;

public abstract class BaseScalarCoercing implements Coercing {

    @Override
    public Object parseLiteral(Object input) throws CoercingParseLiteralException {
        return (input instanceof StringValue) ? parseValue(((StringValue) input).getValue()) : null;
    }
}
