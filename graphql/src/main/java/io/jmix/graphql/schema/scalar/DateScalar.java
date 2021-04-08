package io.jmix.graphql.schema.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import static io.jmix.graphql.schema.scalar.CustomScalars.SERIALIZATION_DATE_FORMAT;


public class DateScalar extends GraphQLScalarType{

    static final Logger log = LoggerFactory.getLogger(DateScalar.class);

    public DateScalar() {
        super("Date", "Date type", new Coercing() {

            @Override
            public Object serialize(Object input) {
                log.info("serialize {}", input);

                if (input instanceof Date) {
                    return DateTimeFormatter.ISO_INSTANT
                            .format(Instant.ofEpochMilli(((Date) input).getTime()));
                }
                throw new CoercingSerializeException(
                        "Expected type 'Date' but was '" + input.getClass().getSimpleName() + "'.");

            }

            @Override
            public Object parseValue(Object input) {
                return parseLiteral(input);
            }

            @Override
            public Object parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    String value = ((StringValue) input).getValue();
                    if (value.isEmpty()) {
                        return Date.from(Instant.EPOCH);
                    }
                    TemporalAccessor temporalAccessor = DateTimeFormatter.ISO_INSTANT.parse(value);
                    Date date = Date.from(Instant.from(temporalAccessor));

                    String dateString = SERIALIZATION_DATE_FORMAT.format(date);
                    log.info("parseLiteral return {}", dateString);
                    return date;
                }
                throw new CoercingParseLiteralException(
                        "Expected type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");

            }
        });
    }

}
