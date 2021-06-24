package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Time;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


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
                // todo move formats to constant class
                String timeStr = value.isEmpty() ? "00:00:00" : value;
                return Time.valueOf(timeStr);
            }
        });
    }

}
