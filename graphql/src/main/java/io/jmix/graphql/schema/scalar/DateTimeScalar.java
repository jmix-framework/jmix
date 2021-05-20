package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.jmix.graphql.schema.scalar.CustomScalars.SERIALIZATION_DATETIME_FORMAT;


public class DateTimeScalar extends GraphQLScalarType {

    static final Logger log = LoggerFactory.getLogger(DateTimeScalar.class);

    public DateTimeScalar() {
        super("DateTime", "Date type with time", new BaseDateCoercing(LocalDateTimeScalar.LOCAL_DATE_TIME_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);

                if (input instanceof Date) {
                    Date date = (Date) input;

                    return DateTimeFormatter.ISO_LOCAL_DATE_TIME
                            .withZone(ZoneId.systemDefault())
                            .format(Instant.ofEpochMilli(date.getTime()));
                }
                throw new CoercingSerializeException(
                        "Expected type 'Date' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return Date.from(Instant.EPOCH);
                }
                Instant temporalAccessor = LocalDateTime.parse(value)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
                Date date = Date.from(temporalAccessor);

                String dateString = SERIALIZATION_DATETIME_FORMAT.format(date);
                log.debug("parseLiteral return {}", dateString);
                return date;
            }
        });
    }

}
