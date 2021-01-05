package io.jmix.graphql.schema.scalar;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static io.jmix.graphql.schema.scalar.CustomScalars.SERIALIZATION_DATE_FORMAT;

public class LocalDateTimeScalar extends GraphQLScalarType{

    static final Logger log = LoggerFactory.getLogger(LocalDateTimeScalar.class);
    public static final String LOCAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    public LocalDateTimeScalar() {
        super("LocalDateTime", "Date type", new Coercing() {

            @Override
            public Object serialize(Object input) {
                log.info("serialize {}", input);
                if (input instanceof LocalDateTime) {
                    LocalDateTime localDateTime = (LocalDateTime) input;
                    return DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_FORMAT).format(localDateTime);
                }
                throw new CoercingSerializeException(
                        "Expected type 'LocalDateTime' but was '" + input.getClass().getSimpleName() + "'.");

            }

            @Override
            public Object parseValue(Object input) {
                return serialize(input);
            }

            @Override
            public Object parseLiteral(Object input) {
                if (input instanceof StringValue) {
                    String value = ((StringValue) input).getValue();
                    try {
                        Date date = new SimpleDateFormat(LOCAL_DATE_TIME_FORMAT).parse(value);
                        String dateString = SERIALIZATION_DATE_FORMAT.format(date);
                        log.info("parseLiteral return {}", dateString);
                        return dateString;
                    } catch (ParseException e) {
                        throw new CoercingSerializeException(e);
                    }

                }
                throw new CoercingSerializeException(
                        "Expected type 'StringValue' but was '" + input.getClass().getSimpleName() + "'.");

            }
        });
    }

}
