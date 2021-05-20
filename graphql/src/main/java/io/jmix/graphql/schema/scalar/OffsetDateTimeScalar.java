package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeScalar extends GraphQLScalarType {

    public static final String OFFSET_DATE_TIME_FORMAT = "'yyyy-MM-dd'T'HH:mm:ss+hh:mm'";
    static final Logger log = LoggerFactory.getLogger(OffsetDateTimeScalar.class);

    public OffsetDateTimeScalar() {
        super("OffsetDateTime", "Date type", new BaseDateCoercing(OFFSET_DATE_TIME_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);
                if (input instanceof OffsetDateTime) {
                    OffsetDateTime offsetDateTime = (OffsetDateTime) input;
                    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(offsetDateTime);
                }
                throw new CoercingSerializeException(
                        "Expected type 'OffsetDateTime' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return OffsetDateTime.MIN;
                }
                try {
                    OffsetDateTime offsetDateTime = OffsetDateTime.from(
                            Instant.from(
                                    DateTimeFormatter
                                            .ISO_OFFSET_DATE_TIME
                                            .parse(value)
                            ).atZone(ZoneId.systemDefault())
                    );
                    log.debug("parseLiteral return {}", offsetDateTime.toString());
                    return offsetDateTime;
                } catch (DateTimeException e) {
                    throw new CoercingParseLiteralException(e);
                }
            }
        });
    }

}
