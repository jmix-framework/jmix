package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
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
        super("Time", "Time type", new BaseDateCoercing(LocalTimeScalar.LOCAL_TIME_FORMAT) {

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
                    return new Date(
                            DateTimeFormatter.ISO_TIME
                                    .parse("00:00:00", LocalTime::from)
                                    .getSecond()
                    );
                }
                try {
                    Date date = DateUtils.parseDate(value.trim(), SERIALIZATION_TIME_FORMAT.toPattern());
                    String dateString = SERIALIZATION_TIME_FORMAT.format(date);
                    log.debug("parseLiteral return {}", dateString);
                    return date;
                } catch (ParseException e) {
                    throw new CoercingParseLiteralException(e);
                }
            }
        });
    }

}
