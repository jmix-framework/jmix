package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeScalar extends GraphQLScalarType {

    public static final String LOCAL_TIME_FORMAT = "'HH:mm:ss' or 'HH:mm'";
    static final Logger log = LoggerFactory.getLogger(LocalTimeScalar.class);

    public LocalTimeScalar() {
        super("LocalTime", "Date type", new BaseDateCoercing(LOCAL_TIME_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);
                if (input instanceof LocalTime) {
                    LocalTime localTime = (LocalTime) input;
                    return DateTimeFormatter.ISO_LOCAL_TIME.format(localTime);
                }
                throw new CoercingSerializeException(
                        "Expected type 'LocalTime' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return LocalTime.MIN;
                }
                try {
                    LocalTime localTime = LocalTime.from(
                            DateTimeFormatter
                                    .ISO_LOCAL_TIME
                                    .parse(value)
                    );
                    log.debug("parseLiteral return {}", localTime.toString());
                    return localTime;
                } catch (DateTimeException e) {
                    throw new CoercingParseLiteralException(e);
                }
            }
        });
    }

}
