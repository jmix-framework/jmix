package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.jmix.graphql.schema.scalar.CustomScalars.SERIALIZATION_DATE_FORMAT;


public class DateScalar extends GraphQLScalarType {

    static final Logger log = LoggerFactory.getLogger(DateScalar.class);

    public DateScalar() {
        super("Date", "Date type", new BaseDateCoercing(LocalDateScalar.LOCAL_DATE_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);

                if (input instanceof Date) {
                    Date date = (Date) input;
                    return DateTimeFormatter.ISO_LOCAL_DATE
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
                Instant temporalAccessor = LocalDate.parse(value)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant();
                Date date = Date.from(temporalAccessor);

                String dateString = SERIALIZATION_DATE_FORMAT.format(date);
                log.debug("parseLiteral return {}", dateString);
                return date;
            }
        });
    }

}
