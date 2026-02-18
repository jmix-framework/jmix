package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeScalar extends GraphQLScalarType {

    public static final String LOCAL_DATE_TIME_FORMAT = "'yyyy-MM-dd'T'HH:mm:ss'";

    public LocalDateTimeScalar() {
        super("LocalDateTime", "Local date type with time", new BaseDateCoercing<LocalDateTime, String>(LOCAL_DATE_TIME_FORMAT, LocalDateTime.class) {

            @Override
            public String doSerialize(LocalDateTime input) {
                return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(input);
            }

            protected LocalDateTime parseString(String value) {
                return LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(value));
            }
        });
    }

}
