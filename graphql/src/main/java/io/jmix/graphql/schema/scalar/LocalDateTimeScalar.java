package io.jmix.graphql.schema.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeScalar extends GraphQLScalarType {

    public static final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    static final Logger log = LoggerFactory.getLogger(LocalDateTimeScalar.class);

    public LocalDateTimeScalar() {
        super("LocalDateTime", "Date type", new Coercing() {

            @Override
            public Object serialize(Object input) {
                log.info("serialize {}", input);
                if (input instanceof LocalDateTime) {
                    LocalDateTime localDateTime = (LocalDateTime) input;
                    return DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT).format(localDateTime);
                }
                throw new CoercingSerializeException(
                        "Expected type 'LocalDateTime' but was '" + input.getClass().getSimpleName() + "'.");

            }

            @Override
            public Object parseValue(Object input) {
                return parseLiteral(input);
            }

            @Override
            public Object parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    String value = ((StringValue) input).getValue();
                    if (value.isEmpty()) {
                        return LocalDateTime.MIN;
                    }
                    try {
                        LocalDateTime localDateTime = LocalDateTime.from(
                                DateTimeFormatter
                                        .ofPattern(LOCAL_DATE_TIME_FORMAT)
                                        .parse(value)
                        );
                        log.info("parseLiteral return {}", localDateTime.toString());
                        return localDateTime;
                    } catch (DateTimeException e) {
                        throw new CoercingParseLiteralException(e);
                    }

                }
                throw new CoercingParseLiteralException(
                        "Expected type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");

            }
        });
    }

}
