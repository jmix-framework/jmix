package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.graphql.schema.scalar.coercing.DatatypeCoercing;

import java.time.OffsetTime;

public class OffsetTimeScalar extends GraphQLScalarType {

    public OffsetTimeScalar(Datatype<OffsetTime> datatype) {
        super("OffsetTime", "Offset time type",
                new DatatypeCoercing<>(OffsetTime.class, datatype,
                        "Please use the format 'HH:mm:ss+hh:mm' or 'HH:mm+hh:mm'"));
    }
}
