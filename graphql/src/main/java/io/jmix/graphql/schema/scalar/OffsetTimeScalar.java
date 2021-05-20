package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import io.jmix.graphql.schema.scalar.coercing.BaseDateCoercing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DateTimeException;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class OffsetTimeScalar extends GraphQLScalarType {

    public static final String OFFSET_TIME_FORMAT = "'HH:mm:ss+hh:mm' or 'HH:mm+hh:mm'";
    static final Logger log = LoggerFactory.getLogger(OffsetTimeScalar.class);

    public OffsetTimeScalar() {
        super("OffsetTime", "Date type", new BaseDateCoercing(OFFSET_TIME_FORMAT) {

            @Override
            public Object serialize(Object input) {
                log.debug("serialize {}", input);
                if (input instanceof OffsetTime) {
                    OffsetTime offsetTime = (OffsetTime) input;
                    return DateTimeFormatter.ISO_OFFSET_TIME.format(offsetTime);
                }
                throw new CoercingSerializeException(
                        "Expected type 'OffsetTime' but was '" + input.getClass().getSimpleName() + "'.");

            }

            protected Object parseString(String value) {
                if (value.isEmpty()) {
                    return OffsetTime.MIN;
                }
                try {
                    OffsetTime offsetTime = OffsetTime.from(
                            DateTimeFormatter
                                    .ISO_OFFSET_TIME
                                    .parse(value)

                    );
                    log.debug("parseLiteral return {}", offsetTime.toString());
                    return offsetTime;
                } catch (DateTimeException e) {
                    throw new CoercingParseLiteralException(e);
                }
            }
        });
    }

}
