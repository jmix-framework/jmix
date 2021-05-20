package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeScalar extends GraphQLScalarType {

    public static final String LOCAL_DATE_TIME_FORMAT = "'yyyy-MM-dd'T'HH:mm:ss'";
    static final Logger log = LoggerFactory.getLogger(LocalDateTimeScalar.class);

    public LocalDateTimeScalar() {
        super("LocalDateTime", "Date type", new BaseDateCoercing(LOCAL_DATE_TIME_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);
                if (input instanceof LocalDateTime) {
                    LocalDateTime localDateTime = (LocalDateTime) input;
                    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(localDateTime);
                }
                throw new CoercingSerializeException(
                        "Expected type 'LocalDateTime' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return LocalDateTime.MIN;
                }
                try {
                    LocalDateTime localDateTime = LocalDateTime.from(
                            DateTimeFormatter
                                    .ISO_LOCAL_DATE_TIME
                                    .parse(value)
                    );
                    log.debug("parseLiteral return {}", localDateTime.toString());
                    return localDateTime;
                } catch (DateTimeException e) {
                    throw new CoercingParseLiteralException(e);
                }
            }
        });
    }

}
