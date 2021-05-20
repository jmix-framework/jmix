package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateScalar extends GraphQLScalarType {

    public static final String LOCAL_DATE_FORMAT = "'yyyy-MM-dd'";
    static final Logger log = LoggerFactory.getLogger(LocalDateScalar.class);

    public LocalDateScalar() {
        super("LocalDate", "Date type", new BaseDateCoercing(LOCAL_DATE_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);
                if (input instanceof LocalDate) {
                    LocalDate localDate = (LocalDate) input;
                    return DateTimeFormatter.ISO_LOCAL_DATE.format(localDate);
                }
                throw new CoercingSerializeException(
                        "Expected type 'LocalDate' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return LocalDate.MIN;
                }
                try {
                    LocalDate localDate = LocalDate.from(
                            DateTimeFormatter
                                    .ISO_LOCAL_DATE
                                    .parse(value)
                    );
                    log.debug("parseLiteral return {}", localDate.toString());
                    return localDate;
                } catch (DateTimeException e) {
                    throw new CoercingParseLiteralException(e);
                }
            }
        });
    }

}
