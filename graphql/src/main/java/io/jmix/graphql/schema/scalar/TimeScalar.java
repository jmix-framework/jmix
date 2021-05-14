package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.jmix.graphql.schema.scalar.CustomScalars.SERIALIZATION_TIME_FORMAT;


public class TimeScalar extends GraphQLScalarType {

    static final Logger log = LoggerFactory.getLogger(TimeScalar.class);

    public TimeScalar() {
        super("Time", "Time type", new BaseDateCoercing() {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);

                if (input instanceof Date) {
                    Date date = (Date) input;
                    return Instant.ofEpochMilli(date.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime().format(DateTimeFormatter.ISO_TIME);
                }
                throw new CoercingSerializeException(
                        "Expected type 'Date' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return dateFromString(LocalTime.MIN.toString());
                }
                Date date = dateFromString(value);

                String dateString = SERIALIZATION_TIME_FORMAT.format(date);
                log.debug("parseLiteral return {}", dateString);
                return date;
            }

            private Date dateFromString(String value) {
                try {
                    return DateUtils.parseDate(value.trim(), SERIALIZATION_TIME_FORMAT.toPattern());
                } catch (ParseException e) {
                    log.error(e.getMessage());
                }
                return new Date(
                        DateTimeFormatter.ISO_TIME
                                .parse("00:00:00", LocalTime::from)
                                .getSecond()
                );
            }
        });
    }

}
