package io.jmix.graphql.schema.scalar;

import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeScalar extends GraphQLScalarType {

    public static final String OFFSET_DATE_TIME_FORMAT = "'yyyy-MM-dd'T'HH:mm:ss+hh:mm'";

    public OffsetDateTimeScalar() {
        super("OffsetDateTime", "Date type with offset", new BaseDateCoercing<OffsetDateTime, String>(OFFSET_DATE_TIME_FORMAT, OffsetDateTime.class) {

            @Override
            public String doSerialize(OffsetDateTime input) {
                return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(input);
            }

            protected OffsetDateTime parseString(String value) {
                return OffsetDateTime.from(
                        Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value))
                                .atZone(ZoneId.systemDefault()));
            }
        });
    }

}
