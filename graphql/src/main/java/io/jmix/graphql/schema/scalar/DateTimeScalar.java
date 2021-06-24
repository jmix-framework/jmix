package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class DateTimeScalar extends GraphQLScalarType {

    static final Logger log = LoggerFactory.getLogger(DateTimeScalar.class);

    public DateTimeScalar() {
        super("DateTime", "Date type with time", new BaseDateCoercing(LocalDateTimeScalar.LOCAL_DATE_TIME_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);

                if (input instanceof Date) {
                    Date date = (Date) input;

                    // todo move formats to constant class
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    return format.format(date);
                }
                throw new CoercingSerializeException(
                        "Expected type 'Date' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return new Timestamp(Date.from(Instant.EPOCH).getTime());
                }

                LocalDateTime localDateTime = DateTimeFormatter.ISO_DATE_TIME.parse(value.trim(), LocalDateTime::from);

                log.debug("parseLiteral return {}", localDateTime);
                
                return Timestamp.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        });
    }

}
