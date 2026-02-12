package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class DateTimeScalar extends GraphQLScalarType {

    public DateTimeScalar() {
        super("DateTime", "Date type with time", new BaseDateCoercing<Date, String>(LocalDateTimeScalar.LOCAL_DATE_TIME_FORMAT, Date.class) {

            @Override
            public String doSerialize(Date input) {
                // todo move formats to constant class
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(input);
            }

            protected Date parseString(String value) {
                LocalDateTime localDateTime = DateTimeFormatter.ISO_DATE_TIME.parse(value.trim(), LocalDateTime::from);
                return Timestamp.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        });
    }

}
