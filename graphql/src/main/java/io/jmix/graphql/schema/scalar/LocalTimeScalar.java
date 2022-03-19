package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.graphql.schema.scalar.coercing.DatatypeCoercing;

import java.time.LocalTime;

public class LocalTimeScalar extends GraphQLScalarType {

    public LocalTimeScalar(Datatype<LocalTime> datatype) {
        super("LocalTime", "Local time type",
                new DatatypeCoercing<>(LocalTime.class, datatype,
                        "Please use the format 'HH:mm:ss' or 'HH:mm'"));
    }

}
