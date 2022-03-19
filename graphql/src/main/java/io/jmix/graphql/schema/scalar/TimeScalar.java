package io.jmix.graphql.schema.scalar;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.GraphQLScalarType;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.graphql.schema.scalar.coercing.DatatypeCoercing;

import java.sql.Time;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;


public class TimeScalar extends GraphQLScalarType {

    public TimeScalar(Datatype<LocalTime> localTimeDatatype, Datatype<Time> timeDatatype) {
        super("Time", "Time type",
                new DatatypeCoercing<Time>(Time.class, timeDatatype,
                        "Please use the format 'HH:mm:ss' or 'HH:mm'") {

                    // a bit more complicated logic, copied from Time parsing method of jmix-rest addon
                    @Override
                    public Time parseValue(Object input) {
                        if (input instanceof String) {
                            try {
                                LocalTime localTime = localTimeDatatype.parse((String) input);
                                if (localTime == null) {
                                    return null;
                                }
                                return  Time.valueOf(localTime);
                            } catch (DateTimeParseException | ParseException exception) {
                                throw new CoercingParseLiteralException(incorrectFormatMessage);
                            }
                        }
                        throw new CoercingParseLiteralException(
                                "Expected type 'String' but was '" + input.getClass().getSimpleName() + "'.");
                    }
                });
    }

}
